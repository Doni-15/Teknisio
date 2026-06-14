package com.teknisio.mobile.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.gson.Gson;
import com.teknisio.mobile.R;
import com.teknisio.mobile.local.TokenManager;
import com.teknisio.mobile.util.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

/**
 * Foreground service that:
 * <ol>
 *   <li>Gets GPS position using {@link FusedLocationProviderClient} every 5 seconds</li>
 *   <li>Sends location updates to the backend via a raw WebSocket (STOMP-compatible frames)</li>
 * </ol>
 *
 * <p>Started by the technician when the status changes to ACCEPTED.
 * Should be stopped when the order moves out of ACCEPTED state.</p>
 */
public class LocationSharingService extends Service {

    private static final String TAG = "LocationSharingService";

    // Intent extras
    public static final String EXTRA_SERVICE_REQUEST_ID = "service_request_id";
    public static final String EXTRA_TOKEN = "auth_token";

    // Notification
    private static final String CHANNEL_ID = "teknisio_location_channel";
    private static final int NOTIFICATION_ID = 1001;

    // Location update interval
    private static final long LOCATION_INTERVAL_MS = 5_000L;
    private static final long LOCATION_FASTEST_INTERVAL_MS = 3_000L;

    // STOMP frame skeleton for sending
    private static final String STOMP_CONNECT =
            "CONNECT\naccept-version:1.2\nheart-beat:10000,10000\n";
    private static final String STOMP_SEND_TPL =
            "SEND\ndestination:/app/location/update/%s\ncontent-type:application/json\n\n%s\u0000";

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private WebSocket webSocket;
    private OkHttpClient okHttpClient;

    private String serviceRequestId;
    private String authToken;
    private boolean stompConnected = false;

    // ─────────────────────────────────────────────────────────────────────────
    // Lifecycle
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public void onCreate() {
        super.onCreate();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        okHttpClient = new OkHttpClient.Builder()
                .pingInterval(20, TimeUnit.SECONDS)
                .build();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            serviceRequestId = intent.getStringExtra(EXTRA_SERVICE_REQUEST_ID);
            authToken = intent.getStringExtra(EXTRA_TOKEN);
        }

        if (authToken == null || authToken.isBlank()) {
            authToken = new TokenManager(this).getAccessToken();
        }

        if (serviceRequestId == null || serviceRequestId.isBlank()) {
            Log.e(TAG, "No service request id — stopping service");
            stopSelf();
            return START_NOT_STICKY;
        }

        startForeground(NOTIFICATION_ID, buildNotification());
        connectWebSocket();
        startLocationUpdates();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
        disconnectWebSocket();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // WebSocket (plain — compatible with Spring STOMP server)
    // ─────────────────────────────────────────────────────────────────────────

    private void connectWebSocket() {
        String wsUrl = buildWebSocketUrl();
        Log.d(TAG, "Connecting WebSocket: " + wsUrl);

        Request request = new Request.Builder()
                .url(wsUrl)
                .addHeader("Authorization", "Bearer " + (authToken != null ? authToken : ""))
                .build();

        webSocket = okHttpClient.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                Log.d(TAG, "WebSocket opened");
                // Send STOMP CONNECT frame with JWT token for backend WebSocket authentication
                String stompToken = (authToken != null && !authToken.isBlank()) ? authToken : "";
                webSocket.send(STOMP_CONNECT + "Authorization: Bearer " + stompToken + "\n\n\u0000");
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                Log.d(TAG, "WS message: " + text);
                if (text.startsWith("CONNECTED")) {
                    stompConnected = true;
                    Log.d(TAG, "STOMP connected");
                }
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, @Nullable Response response) {
                Log.e(TAG, "WebSocket failure: " + t.getMessage());
                stompConnected = false;
                // Retry after 5 seconds
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    if (serviceRequestId != null) {
                        connectWebSocket();
                    }
                }, 5_000L);
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                stompConnected = false;
                Log.d(TAG, "WebSocket closed: " + reason);
            }
        });
    }

    private void disconnectWebSocket() {
        if (webSocket != null) {
            webSocket.close(1000, "Service stopped");
            webSocket = null;
        }
        stompConnected = false;
    }

    /**
     * Sends a STOMP SEND frame carrying the location JSON payload.
     */
    private void sendLocationViaWebSocket(double latitude, double longitude) {
        if (webSocket == null || !stompConnected) {
            Log.w(TAG, "WebSocket not ready, skipping location send");
            return;
        }

        try {
            JSONObject json = new JSONObject();
            json.put("serviceRequestId", serviceRequestId);
            json.put("latitude", latitude);
            json.put("longitude", longitude);
            json.put("timestamp", System.currentTimeMillis());

            String stompFrame = String.format(STOMP_SEND_TPL, serviceRequestId, json.toString());
            webSocket.send(stompFrame);
            Log.d(TAG, "Location sent: " + latitude + ", " + longitude);
        } catch (JSONException e) {
            Log.e(TAG, "Failed to build location JSON", e);
        }
    }

    private String buildWebSocketUrl() {
        // Convert https:// → wss://, http:// → ws://
        String base = Constants.BASE_URL
                .replace("https://", "wss://")
                .replace("http://", "ws://");

        // Remove trailing slash
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }

        // Use raw WebSocket endpoint (Spring SockJS fallback: /ws/websocket)
        return base + "/ws/websocket";
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Location updates
    // ─────────────────────────────────────────────────────────────────────────

    @SuppressWarnings("MissingPermission")
    private void startLocationUpdates() {
        LocationRequest locationRequest = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                LOCATION_INTERVAL_MS
        )
                .setMinUpdateIntervalMillis(LOCATION_FASTEST_INTERVAL_MS)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) return;
                android.location.Location location = locationResult.getLastLocation();
                if (location != null) {
                    double lat = location.getLatitude();
                    double lng = location.getLongitude();
                    Log.d(TAG, "Location update: " + lat + ", " + lng);

                    // Send via WebSocket
                    sendLocationViaWebSocket(lat, lng);

                    // Broadcast to any listening Activity via LocalBroadcastManager
                    Intent intent = new Intent("teknisio.location.update");
                    intent.putExtra("latitude", lat);
                    intent.putExtra("longitude", lng);
                    sendBroadcast(intent);
                }
            }
        };

        try {
            fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
            );
        } catch (SecurityException e) {
            Log.e(TAG, "Location permission not granted", e);
            stopSelf();
        }
    }

    private void stopLocationUpdates() {
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Foreground notification
    // ─────────────────────────────────────────────────────────────────────────

    private Notification buildNotification() {
        createNotificationChannel();

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Teknisio — Berbagi Lokasi")
                .setContentText("Lokasi Anda sedang dibagikan ke pelanggan")
                .setSmallIcon(android.R.drawable.ic_menu_compass)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .build();
    }

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Berbagi Lokasi GPS",
                NotificationManager.IMPORTANCE_LOW
        );
        channel.setDescription("Digunakan saat teknisi sedang menuju lokasi pelanggan");

        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(channel);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Static helper
    // ─────────────────────────────────────────────────────────────────────────

    public static void start(Context context, String serviceRequestId, String authToken) {
        Intent intent = new Intent(context, LocationSharingService.class);
        intent.putExtra(EXTRA_SERVICE_REQUEST_ID, serviceRequestId);
        intent.putExtra(EXTRA_TOKEN, authToken);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    public static void stop(Context context) {
        context.stopService(new Intent(context, LocationSharingService.class));
    }
}

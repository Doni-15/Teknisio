package com.teknisio.mobile.view.tracking;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.teknisio.mobile.R;
import com.teknisio.mobile.base.BaseActivity;
import com.teknisio.mobile.local.TokenManager;
import com.teknisio.mobile.model.response.ApiResponse;
import com.teknisio.mobile.network.ApiClient;
import com.teknisio.mobile.service.LocationSharingService;
import com.teknisio.mobile.util.AppToast;
import com.teknisio.mobile.util.BackButtonHelper;
import com.teknisio.mobile.util.Constants;

import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * Full-screen GPS tracking map activity.
 *
 * <p>Two modes:
 * <ul>
 *   <li><b>MODE_CUSTOMER</b>: subscribes to technician location via WebSocket and shows a moving pin</li>
 *   <li><b>MODE_TECHNICIAN</b>: starts {@link LocationSharingService} and shows the customer's destination pin</li>
 * </ul>
 * </p>
 */
public class TrackingMapActivity extends BaseActivity {

    private static final String TAG = "TrackingMapActivity";

    public static final String EXTRA_MODE = "mode";
    public static final String EXTRA_SERVICE_REQUEST_ID = "service_request_id";
    public static final String EXTRA_CUSTOMER_LAT = "customer_lat";
    public static final String EXTRA_CUSTOMER_LNG = "customer_lng";
    public static final String EXTRA_TECHNICIAN_NAME = "technician_name";

    public static final String MODE_CUSTOMER = "CUSTOMER";
    public static final String MODE_TECHNICIAN = "TECHNICIAN";

    private static final int PERMISSION_REQUEST_CODE = 2001;
    private static final long POLLING_INTERVAL_MS = 5_000L;

    // UI
    private MapView mapView;
    private TextView txtTrackingTitle;
    private TextView txtTrackingStatus;
    private TextView txtLiveBadge;
    private TextView txtTechnicianName;
    private TextView txtLastUpdate;
    private TextView txtDistance;
    private TextView txtInfoHint;
    private FloatingActionButton btnCenterMap;

    // State
    private String mode;
    private String serviceRequestId;
    private double customerLat;
    private double customerLng;
    private String technicianName;

    // Map overlays
    private Marker technicianMarker;
    private Marker customerMarker;
    private MyLocationNewOverlay myLocationOverlay;

    // WebSocket (customer mode — subscribe to technician location)
    private WebSocket subscriberWebSocket;
    private OkHttpClient wsClient;

    // Polling fallback
    private final Handler pollingHandler = new Handler(Looper.getMainLooper());
    private boolean pollingActive = false;

    // Broadcast receiver (technician mode — listen to LocationSharingService)
    private BroadcastReceiver locationBroadcastReceiver;

    // Last known technician position (for distance calculation)
    private GeoPoint lastTechnicianPoint;

    // ─────────────────────────────────────────────────────────────────────────
    // Lifecycle
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // OSMDroid must be configured before setContentView
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        Configuration.getInstance().setUserAgentValue(getPackageName());

        setContentView(R.layout.activity_tracking_map);

        // Parse intent
        mode = getIntent().getStringExtra(EXTRA_MODE);
        serviceRequestId = getIntent().getStringExtra(EXTRA_SERVICE_REQUEST_ID);
        customerLat = getIntent().getDoubleExtra(EXTRA_CUSTOMER_LAT, 0);
        customerLng = getIntent().getDoubleExtra(EXTRA_CUSTOMER_LNG, 0);
        technicianName = getIntent().getStringExtra(EXTRA_TECHNICIAN_NAME);

        bindViews();
        setupMap();
        setupActions();
        applyMode();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mapView != null) mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mapView != null) mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopPolling();
        disconnectSubscriberWebSocket();
        unregisterLocationReceiver();

        if (MODE_TECHNICIAN.equals(mode)) {
            LocationSharingService.stop(this);
        }

        if (mapView != null) {
            mapView.onDetach();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Setup
    // ─────────────────────────────────────────────────────────────────────────

    private void bindViews() {
        mapView = findViewById(R.id.mapView);
        txtTrackingTitle = findViewById(R.id.txtTrackingTitle);
        txtTrackingStatus = findViewById(R.id.txtTrackingStatus);
        txtLiveBadge = findViewById(R.id.txtLiveBadge);
        txtTechnicianName = findViewById(R.id.txtTechnicianName);
        txtLastUpdate = findViewById(R.id.txtLastUpdate);
        txtDistance = findViewById(R.id.txtDistance);
        txtInfoHint = findViewById(R.id.txtInfoHint);
        btnCenterMap = findViewById(R.id.btnCenterMap);

        android.widget.FrameLayout btnBack = findViewById(R.id.btnBack);
        BackButtonHelper.setup(btnBack, this::finish);
    }

    private void setupMap() {
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(15.0);

        // Compass overlay
        CompassOverlay compassOverlay = new CompassOverlay(this, mapView);
        compassOverlay.enableCompass();
        mapView.getOverlays().add(compassOverlay);
    }

    private void setupActions() {
        btnCenterMap.setOnClickListener(v -> centerMap());
    }

    private void applyMode() {
        if (MODE_TECHNICIAN.equals(mode)) {
            setupTechnicianMode();
        } else {
            setupCustomerMode();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Customer Mode — view technician's moving location
    // ─────────────────────────────────────────────────────────────────────────

    private void setupCustomerMode() {
        txtTrackingTitle.setText("Lacak Teknisi");
        txtInfoHint.setText("Teknisi sedang menuju lokasi Anda");
        if (technicianName != null && !technicianName.isBlank()) {
            txtTechnicianName.setText(technicianName);
        }

        // Show customer's own location pin (destination for the technician)
        if (customerLat != 0 && customerLng != 0) {
            addCustomerPin(new GeoPoint(customerLat, customerLng));
            mapView.getController().animateTo(new GeoPoint(customerLat, customerLng));
        }

        // Try WebSocket first, fall back to polling
        connectCustomerWebSocket();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Technician Mode — share own location + see customer destination
    // ─────────────────────────────────────────────────────────────────────────

    private void setupTechnicianMode() {
        txtTrackingTitle.setText("Navigasi ke Pelanggan");
        txtInfoHint.setText("Lokasi Anda sedang dibagikan ke pelanggan");

        // Show customer destination pin
        if (customerLat != 0 && customerLng != 0) {
            GeoPoint destination = new GeoPoint(customerLat, customerLng);
            addCustomerPin(destination);
            mapView.getController().animateTo(destination);
        }

        // Show my location overlay
        if (hasLocationPermission()) {
            addMyLocationOverlay();
            startLocationSharing();
        } else {
            requestLocationPermission();
        }

        // Listen to own location broadcasts from the service to update distance
        registerLocationReceiver();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // WebSocket — Customer subscribes to technician location
    // ─────────────────────────────────────────────────────────────────────────

    private void connectCustomerWebSocket() {
        setStatus("Menghubungkan...");

        wsClient = new OkHttpClient.Builder()
                .pingInterval(20, TimeUnit.SECONDS)
                .build();

        String wsUrl = buildWebSocketUrl();
        TokenManager tokenManager = new TokenManager(this);
        String token = tokenManager.getAccessToken();

        Request request = new Request.Builder()
                .url(wsUrl)
                .addHeader("Authorization", "Bearer " + (token != null ? token : ""))
                .build();

        subscriberWebSocket = wsClient.newWebSocket(request, new WebSocketListener() {
            private boolean stompConnected = false;

            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                // STOMP CONNECT
                webSocket.send("CONNECT\naccept-version:1.2\nheart-beat:10000,10000\n\n\u0000");
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                if (text.startsWith("CONNECTED") && !stompConnected) {
                    stompConnected = true;
                    // Subscribe to the technician's location topic
                    String subscribeFrame = "SUBSCRIBE\nid:sub-location\n"
                            + "destination:/topic/location/" + serviceRequestId + "\n\n\u0000";
                    webSocket.send(subscribeFrame);

                    runOnUiThread(() -> {
                        setStatus("Terhubung — menunggu lokasi teknisi...");
                        txtLiveBadge.setVisibility(View.VISIBLE);
                    });

                } else if (text.startsWith("MESSAGE")) {
                    // Extract JSON body (after the blank line separator)
                    int bodyStart = text.indexOf("\n\n");
                    if (bodyStart >= 0) {
                        String body = text.substring(bodyStart + 2).replace("\u0000", "").trim();
                        handleLocationMessage(body);
                    }
                }
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                Log.w(TAG, "Customer WS failed: " + t.getMessage() + " — switching to polling");
                runOnUiThread(() -> {
                    setStatus("Polling lokasi setiap 5 detik...");
                    startPolling();
                });
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                Log.d(TAG, "Customer WS closed: " + reason);
            }
        });
    }

    private void handleLocationMessage(String json) {
        try {
            JSONObject obj = new JSONObject(json);
            double lat = obj.getDouble("latitude");
            double lng = obj.getDouble("longitude");
            GeoPoint techPoint = new GeoPoint(lat, lng);

            runOnUiThread(() -> updateTechnicianMarker(techPoint));
        } catch (JSONException e) {
            Log.e(TAG, "Failed to parse location message: " + json, e);
        }
    }

    private void disconnectSubscriberWebSocket() {
        if (subscriberWebSocket != null) {
            subscriberWebSocket.close(1000, "Activity destroyed");
            subscriberWebSocket = null;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Polling fallback (customer mode)
    // ─────────────────────────────────────────────────────────────────────────

    private void startPolling() {
        pollingActive = true;
        scheduleNextPoll();
    }

    private void stopPolling() {
        pollingActive = false;
        pollingHandler.removeCallbacksAndMessages(null);
    }

    private void scheduleNextPoll() {
        if (!pollingActive) return;

        pollingHandler.postDelayed(() -> {
            pollLocation();
            scheduleNextPoll();
        }, POLLING_INTERVAL_MS);
    }

    private void pollLocation() {
        if (serviceRequestId == null) return;

        ApiClient.getApiService(this)
                .getLastTechnicianLocation(serviceRequestId)
                .enqueue(new Callback<ApiResponse<LocationResponse>>() {
                    @Override
                    public void onResponse(
                            Call<ApiResponse<LocationResponse>> call,
                            retrofit2.Response<ApiResponse<LocationResponse>> response
                    ) {
                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().success
                                && response.body().data != null) {
                            LocationResponse loc = response.body().data;
                            GeoPoint point = new GeoPoint(loc.latitude, loc.longitude);
                            runOnUiThread(() -> updateTechnicianMarker(point));
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<LocationResponse>> call, Throwable t) {
                        Log.w(TAG, "Polling failed: " + t.getMessage());
                    }
                });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Technician Mode — location broadcast from LocationSharingService
    // ─────────────────────────────────────────────────────────────────────────

    private void registerLocationReceiver() {
        locationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                double lat = intent.getDoubleExtra("latitude", 0);
                double lng = intent.getDoubleExtra("longitude", 0);
                if (lat != 0 && lng != 0) {
                    GeoPoint myPoint = new GeoPoint(lat, lng);
                    updateDistanceDisplay(myPoint);
                }
            }
        };
        registerReceiver(locationBroadcastReceiver, new IntentFilter("teknisio.location.update"),
                RECEIVER_NOT_EXPORTED);
    }

    private void unregisterLocationReceiver() {
        if (locationBroadcastReceiver != null) {
            try {
                unregisterReceiver(locationBroadcastReceiver);
            } catch (IllegalArgumentException ignored) {
            }
            locationBroadcastReceiver = null;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Map helpers
    // ─────────────────────────────────────────────────────────────────────────

    private void updateTechnicianMarker(GeoPoint point) {
        lastTechnicianPoint = point;

        if (technicianMarker == null) {
            technicianMarker = new Marker(mapView);
            technicianMarker.setTitle("Teknisi");
            technicianMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            mapView.getOverlays().add(technicianMarker);
        }

        technicianMarker.setPosition(point);
        mapView.invalidate();

        // Update status and distance
        txtLastUpdate.setText("Lokasi diperbarui barusan");
        setStatus("Live — lokasi teknisi aktif");

        // Calculate distance to customer
        if (customerLat != 0 && customerLng != 0) {
            updateDistanceDisplay(point);
        }
    }

    private void updateDistanceDisplay(GeoPoint from) {
        if (customerLat == 0 && customerLng == 0) return;

        Location locFrom = new Location("from");
        locFrom.setLatitude(from.getLatitude());
        locFrom.setLongitude(from.getLongitude());

        Location locTo = new Location("to");
        locTo.setLatitude(customerLat);
        locTo.setLongitude(customerLng);

        float distanceMeters = locFrom.distanceTo(locTo);

        if (distanceMeters < 1000) {
            txtDistance.setText(String.format("%.0f m", distanceMeters));
        } else {
            txtDistance.setText(String.format("%.1f km", distanceMeters / 1000f));
        }
    }

    private void addCustomerPin(GeoPoint point) {
        customerMarker = new Marker(mapView);
        customerMarker.setPosition(point);
        customerMarker.setTitle("Lokasi Pelanggan");
        customerMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        mapView.getOverlays().add(customerMarker);
        mapView.invalidate();
    }

    @SuppressWarnings("MissingPermission")
    private void addMyLocationOverlay() {
        myLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), mapView);
        myLocationOverlay.enableMyLocation();
        myLocationOverlay.enableFollowLocation();
        mapView.getOverlays().add(myLocationOverlay);
    }

    private void centerMap() {
        if (lastTechnicianPoint != null) {
            mapView.getController().animateTo(lastTechnicianPoint);
        } else if (customerLat != 0) {
            mapView.getController().animateTo(new GeoPoint(customerLat, customerLng));
        }
    }

    private void setStatus(String status) {
        if (txtTrackingStatus != null) {
            txtTrackingStatus.setText(status);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Location Sharing Service
    // ─────────────────────────────────────────────────────────────────────────

    private void startLocationSharing() {
        TokenManager tokenManager = new TokenManager(this);
        String token = tokenManager.getAccessToken();
        LocationSharingService.start(this, serviceRequestId, token);
        setStatus("Berbagi lokasi aktif");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Permission handling
    // ─────────────────────────────────────────────────────────────────────────

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                },
                PERMISSION_REQUEST_CODE
        );
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                addMyLocationOverlay();
                startLocationSharing();
            } else {
                AppToast.error(this, "Izin lokasi diperlukan untuk berbagi lokasi.");
                setStatus("Izin lokasi ditolak");
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Utility
    // ─────────────────────────────────────────────────────────────────────────

    private String buildWebSocketUrl() {
        String base = Constants.BASE_URL
                .replace("https://", "wss://")
                .replace("http://", "ws://");
        if (base.endsWith("/")) base = base.substring(0, base.length() - 1);
        return base + "/ws/";
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Simple DTO for polling response
    // ─────────────────────────────────────────────────────────────────────────

    public static class LocationResponse {
        public String serviceRequestId;
        public double latitude;
        public double longitude;
        public String timestamp;
    }
}

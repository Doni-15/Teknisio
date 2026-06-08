package com.teknisio.mobile.view.customer;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.teknisio.mobile.R;
import com.teknisio.mobile.base.BaseActivity;
import com.teknisio.mobile.local.TokenManager;
import com.teknisio.mobile.model.request.SendChatRequest;
import com.teknisio.mobile.model.response.ApiResponse;
import com.teknisio.mobile.model.response.ChatMessageResponse;
import com.teknisio.mobile.network.ApiClient;
import com.teknisio.mobile.util.AppToast;
import com.teknisio.mobile.util.BackButtonHelper;
import com.teknisio.mobile.util.Constants;
import com.teknisio.mobile.util.ErrorParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * Real-time chat screen between customer and technician for a service request.
 *
 * <p>Uses WebSocket/STOMP for real-time messages and REST for history.</p>
 */
public class ChatActivity extends BaseActivity {

    private static final String TAG = "ChatActivity";

    public static final String EXTRA_SERVICE_REQUEST_ID = "extra_service_request_id";
    public static final String EXTRA_CHAT_PARTNER_NAME = "extra_chat_partner_name";

    // UI
    private FrameLayout btnBack;
    private TextView txtChatTitle;
    private TextView txtChatSubtitle;
    private LinearLayout layoutMessages;
    private ScrollView scrollMessages;
    private EditText edtMessage;
    private Button btnSend;

    // State
    private String serviceRequestId;
    private String chatPartnerName;
    private String currentUserId;
    private boolean loadingHistory = false;

    // WebSocket
    private WebSocket chatWebSocket;
    private OkHttpClient wsClient;
    private boolean stompConnected = false;

    // Message list
    private final List<ChatMessageResponse> messages = new ArrayList<>();
    private final Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        serviceRequestId = getIntent().getStringExtra(EXTRA_SERVICE_REQUEST_ID);
        chatPartnerName = getIntent().getStringExtra(EXTRA_CHAT_PARTNER_NAME);

        // Get current user ID from TokenManager
        TokenManager tokenManager = new TokenManager(this);
        currentUserId = tokenManager.getUserId();

        if (isBlank(serviceRequestId)) {
            AppToast.error(this, "Data chat tidak valid.");
            finish();
            return;
        }

        bindViews();
        setupActions();
        connectChatWebSocket();
        loadChatHistory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disconnectChatWebSocket();
    }

    private void bindViews() {
        btnBack = findViewById(R.id.btnBack);
        txtChatTitle = findViewById(R.id.txtChatTitle);
        txtChatSubtitle = findViewById(R.id.txtChatSubtitle);
        layoutMessages = findViewById(R.id.layoutMessages);
        scrollMessages = findViewById(R.id.scrollMessages);
        edtMessage = findViewById(R.id.edtMessage);
        btnSend = findViewById(R.id.btnSend);
    }

    private void setupActions() {
        BackButtonHelper.setup(btnBack, this::finish);

        if (chatPartnerName != null && !chatPartnerName.isBlank()) {
            txtChatTitle.setText(chatPartnerName);
        } else {
            txtChatTitle.setText("Chat");
        }
        txtChatSubtitle.setText("Menghubungkan...");

        btnSend.setOnClickListener(v -> sendMessage());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // WebSocket
    // ─────────────────────────────────────────────────────────────────────────

    private void connectChatWebSocket() {
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

        chatWebSocket = wsClient.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                // STOMP CONNECT
                webSocket.send("CONNECT\naccept-version:1.2\nheart-beat:10000,10000\n\n\u0000");
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                if (text.startsWith("CONNECTED") && !stompConnected) {
                    stompConnected = true;
                    // Subscribe to the chat topic for this service request
                    String subscribeFrame = "SUBSCRIBE\nid:sub-chat\n"
                            + "destination:/topic/chat/" + serviceRequestId + "\n\n\u0000";
                    webSocket.send(subscribeFrame);
                    runOnUiThread(() -> txtChatSubtitle.setText("Online"));
                } else if (text.startsWith("MESSAGE")) {
                    int bodyStart = text.indexOf("\n\n");
                    if (bodyStart >= 0) {
                        String body = text.substring(bodyStart + 2).replace("\u0000", "").trim();
                        handleIncomingMessage(body);
                    }
                }
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                Log.w(TAG, "Chat WS failed: " + t.getMessage());
                runOnUiThread(() -> txtChatSubtitle.setText("Terputus — coba lagi nanti"));
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                stompConnected = false;
            }
        });
    }

    private void handleIncomingMessage(String json) {
        try {
            ChatMessageResponse msg = gson.fromJson(json, ChatMessageResponse.class);
            if (msg != null && !isBlank(msg.message)) {
                runOnUiThread(() -> {
                    messages.add(msg);
                    addMessageBubble(msg);
                    scrollToBottom();
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to parse incoming chat message", e);
        }
    }

    private void disconnectChatWebSocket() {
        if (chatWebSocket != null) {
            chatWebSocket.close(1000, "Activity destroyed");
            chatWebSocket = null;
        }
        stompConnected = false;
    }

    private void sendMessageViaWebSocket(String message) {
        if (chatWebSocket == null || !stompConnected) {
            AppToast.warning(this, "Belum terhubung ke server chat.");
            return;
        }

        try {
            JSONObject json = new JSONObject();
            json.put("serviceRequestId", serviceRequestId);
            json.put("message", message);

            String stompFrame = "SEND\ndestination:/app/chat/send/" + serviceRequestId
                    + "\ncontent-type:application/json\n\n" + json.toString() + "\u0000";
            chatWebSocket.send(stompFrame);
        } catch (JSONException e) {
            Log.e(TAG, "Failed to build chat message JSON", e);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // REST — load history
    // ─────────────────────────────────────────────────────────────────────────

    private void loadChatHistory() {
        loadingHistory = true;
        ApiClient.getApiService(this)
                .getChatHistory(serviceRequestId)
                .enqueue(new Callback<ApiResponse<List<ChatMessageResponse>>>() {
                    @Override
                    public void onResponse(
                            Call<ApiResponse<List<ChatMessageResponse>>> call,
                            retrofit2.Response<ApiResponse<List<ChatMessageResponse>>> response
                    ) {
                        loadingHistory = false;
                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().success
                                && response.body().data != null) {
                            messages.clear();
                            messages.addAll(response.body().data);
                            renderMessages();
                        }
                        // Mark messages as read
                        markMessagesAsRead();
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<ChatMessageResponse>>> call, Throwable t) {
                        loadingHistory = false;
                    }
                });
    }

    private void markMessagesAsRead() {
        ApiClient.getApiService(this)
                .markChatAsRead(serviceRequestId)
                .enqueue(new Callback<ApiResponse<Void>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Void>> call,
                                           retrofit2.Response<ApiResponse<Void>> response) {}
                    @Override
                    public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {}
                });
    }

    private void sendMessage() {
        String message = edtMessage.getText().toString().trim();
        if (message.isEmpty()) return;

        edtMessage.setText("");
        sendMessageViaWebSocket(message);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UI rendering
    // ─────────────────────────────────────────────────────────────────────────

    private void renderMessages() {
        layoutMessages.removeAllViews();
        for (ChatMessageResponse msg : messages) {
            addMessageBubble(msg);
        }
        scrollToBottom();
    }

    private void addMessageBubble(ChatMessageResponse msg) {
        boolean isMine = currentUserId != null && currentUserId.equals(msg.senderId);

        LinearLayout bubble = new LinearLayout(this);
        bubble.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams bubbleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        bubbleParams.setMargins(dp(8), dp(4), dp(8), dp(4));
        bubbleParams.gravity = isMine ? Gravity.END : Gravity.START;
        bubble.setLayoutParams(bubbleParams);
        bubble.setPadding(dp(12), dp(8), dp(12), dp(8));
        bubble.setBackgroundResource(isMine
                ? R.drawable.bg_chat_bubble_mine
                : R.drawable.bg_chat_bubble_theirs);

        // Sender name (only for received messages)
        if (!isMine && !isBlank(msg.senderName)) {
            TextView senderName = new TextView(this);
            senderName.setText(msg.senderName);
            senderName.setTextColor(Color.parseColor("#6B7680"));
            senderName.setTextSize(11);
            senderName.setTypeface(Typeface.DEFAULT_BOLD);
            LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            nameParams.setMargins(0, 0, 0, dp(4));
            senderName.setLayoutParams(nameParams);
            bubble.addView(senderName);
        }

        // Message text
        TextView messageText = new TextView(this);
        messageText.setText(msg.message);
        messageText.setTextColor(isMine ? Color.WHITE : Color.parseColor("#1F2329"));
        messageText.setTextSize(15);
        messageText.setMaxWidth(dp(260));
        bubble.addView(messageText);

        // Time
        TextView timeText = new TextView(this);
        timeText.setText(formatTime(msg.sentAt));
        timeText.setTextColor(isMine ? Color.parseColor("#B0D4F1") : Color.parseColor("#A0AAB4"));
        timeText.setTextSize(11);
        LinearLayout.LayoutParams timeParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        timeParams.setMargins(0, dp(4), 0, 0);
        timeParams.gravity = Gravity.END;
        timeText.setLayoutParams(timeParams);
        bubble.addView(timeText);

        layoutMessages.addView(bubble);
    }

    private void scrollToBottom() {
        scrollMessages.post(() -> scrollMessages.fullScroll(View.FOCUS_DOWN));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Utility
    // ─────────────────────────────────────────────────────────────────────────

    private String buildWebSocketUrl() {
        String base = Constants.BASE_URL
                .replace("https://", "wss://")
                .replace("http://", "ws://");
        if (base.endsWith("/")) base = base.substring(0, base.length() - 1);
        return base + "/ws/websocket";
    }

    private String formatTime(String isoTime) {
        if (isBlank(isoTime)) return "";
        try {
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX", Locale.US);
            isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = isoFormat.parse(isoTime);

            SimpleDateFormat displayFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            if (date != null) return displayFormat.format(date);
        } catch (Exception ignored) {}
        return "";
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
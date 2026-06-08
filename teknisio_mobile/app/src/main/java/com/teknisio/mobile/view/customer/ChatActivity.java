package com.teknisio.mobile.view.customer;

import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
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
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;

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
    private TextView txtChatAvatarInitial;
    private TextView txtChatSubtitle;
    private TextView txtChatEmptyState;
    private LinearLayout layoutMessages;
    private LinearLayout layoutChatInputBar;
    private ScrollView scrollMessages;
    private EditText edtMessage;
    private Button btnSend;

    // State
    private String serviceRequestId;
    private String chatPartnerName;
    private String currentUserId;
    private boolean loadingHistory = false;
    private int chatInputDefaultBottomMargin = 0;
    private float lastInputBarTranslationY = 0f;

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
        setupResponsiveKeyboardLift();
        connectChatWebSocket();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Always reload history when returning to this screen
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
        txtChatAvatarInitial = findViewById(R.id.txtChatAvatarInitial);
        txtChatSubtitle = findViewById(R.id.txtChatSubtitle);
        txtChatEmptyState = findViewById(R.id.txtChatEmptyState);
        layoutMessages = findViewById(R.id.layoutMessages);
        layoutChatInputBar = findViewById(R.id.layoutChatInputBar);
        scrollMessages = findViewById(R.id.scrollMessages);
        edtMessage = findViewById(R.id.edtMessage);
        btnSend = findViewById(R.id.btnSend);
    }

    private void setupActions() {
        BackButtonHelper.setup(btnBack, this::finish);

        if (chatPartnerName != null && !chatPartnerName.isBlank()) {
            txtChatTitle.setText(chatPartnerName);
        setChatAvatarInitial(chatPartnerName);
        } else {
            txtChatTitle.setText("Chat");
        }
        txtChatSubtitle.setText("● Menghubungkan...");

        // Bold + enable send button only when there is text in the input
        edtMessage.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(android.text.Editable s) {
                boolean hasText = s != null && s.toString().trim().length() > 0;
                updateSendButtonState(hasText);
            }
        });

        // Start disabled
        updateSendButtonState(false);

        btnSend.setOnClickListener(v -> sendMessage());

        edtMessage.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                edtMessage.postDelayed(this::scrollToBottom, 250);
            }
        });

        edtMessage.setOnClickListener(v -> edtMessage.postDelayed(this::scrollToBottom, 250));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // WebSocket
    // ─────────────────────────────────────────────────────────────────────────

    private void setupResponsiveKeyboardLift() {
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING
                        | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN
        );

        View root = findViewById(R.id.rootChat);
        if (root == null || layoutChatInputBar == null || scrollMessages == null) {
            return;
        }

        ViewCompat.setOnApplyWindowInsetsListener(root, (view, insets) -> {
            boolean keyboardVisibleNow = insets.isVisible(WindowInsetsCompat.Type.ime());

            Insets imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime());
            Insets navInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars());

            int floatingLift = 0;

            if (keyboardVisibleNow) {
                // Floating murni: pakai tinggi keyboard asli.
                // Jangan kurangi nav bar karena di beberapa device input jadi terbenam.
                floatingLift = Math.max(0, imeInsets.bottom);

                int maxLift = (int) (getResources().getDisplayMetrics().heightPixels * 0.55f);
                floatingLift = Math.min(floatingLift, maxLift);
            }

            applyKeyboardFloatingLift(floatingLift, keyboardVisibleNow);

            return insets;
        });

        edtMessage.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                ViewCompat.requestApplyInsets(root);
                root.postDelayed(() -> ViewCompat.requestApplyInsets(root), 220);
            }
        });

        edtMessage.setOnClickListener(v -> {
            ViewCompat.requestApplyInsets(root);
            root.postDelayed(() -> ViewCompat.requestApplyInsets(root), 220);
        });

        ViewCompat.requestApplyInsets(root);
    }

    private void animateInputBarTranslation(float targetTranslationY) {
        if (layoutChatInputBar == null) {
            return;
        }

        if (Math.abs(lastInputBarTranslationY - targetTranslationY) < 1f) {
            return;
        }

        lastInputBarTranslationY = targetTranslationY;

        layoutChatInputBar.animate().cancel();
        layoutChatInputBar.animate()
                .translationY(targetTranslationY)
                .setDuration(170)
                .setInterpolator(new android.view.animation.DecelerateInterpolator())
                .start();
    }

    private void applyKeyboardFloatingLift(int floatingLift, boolean keyboardVisibleNow) {
        if (layoutChatInputBar == null || scrollMessages == null) {
            return;
        }

        // Floating effect: hanya input bar yang naik.
        // Tidak pakai margin bottom, jadi tidak mengganggu layout chat.
        animateInputBarTranslation(-floatingLift);

        // Jangan ubah padding scroll saat keyboard muncul.
        // Kalau padding/scroll diubah, chat ikut terdorong ke atas.
        scrollMessages.setPadding(
                scrollMessages.getPaddingLeft(),
                scrollMessages.getPaddingTop(),
                scrollMessages.getPaddingRight(),
                dp(20)
        );
    }

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
                // STOMP CONNECT with JWT token for authentication
                String stompToken = (token != null && !token.isBlank()) ? token : "";
                String connectFrame = "CONNECT\naccept-version:1.2\nheart-beat:10000,10000\nAuthorization: Bearer " + stompToken + "\n\n\u0000";
                webSocket.send(connectFrame);
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                if (text.startsWith("CONNECTED") && !stompConnected) {
                    stompConnected = true;
                    // Subscribe to the chat topic for this service request
                    String subscribeFrame = "SUBSCRIBE\nid:sub-chat\n"
                            + "destination:/topic/chat/" + serviceRequestId + "\n\n\u0000";
                    webSocket.send(subscribeFrame);
                    runOnUiThread(() -> txtChatSubtitle.setText("● Online"));
                } else if (text.startsWith("MESSAGE")) {
                    int bodyStart = text.indexOf("\r\n\r\n");
                    int offset = 4;
                    if (bodyStart < 0) {
                        bodyStart = text.indexOf("\n\n");
                        offset = 2;
                    }
                    if (bodyStart >= 0) {
                        String body = text.substring(bodyStart + offset).replace("\u0000", "").trim();
                        handleIncomingMessage(body);
                    }
                } else if (text.contains("\nmessage:")) {
                    // STOMP ERROR frame — log the reason for debugging
                    Log.e(TAG, "STOMP ERROR received: " + text);
                    runOnUiThread(() -> txtChatSubtitle.setText("Autentikasi gagal — coba login ulang"));
                }
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                Log.w(TAG, "Chat WS failed: " + t.getMessage());
                String errMsg = "Terputus: " + t.getMessage();
                if (response != null) {
                    errMsg += " (HTTP " + response.code() + " " + response.message() + ")";
                }
                final String finalMsg = errMsg;
                runOnUiThread(() -> txtChatSubtitle.setText(finalMsg));
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
                    boolean alreadyExists = false;

                    // 1. Check if message with this chatId already exists in the list
                    if (msg.chatId != null) {
                        for (ChatMessageResponse existing : messages) {
                            if (msg.chatId.equals(existing.chatId)) {
                                alreadyExists = true;
                                break;
                            }
                        }
                    }

                    // 2. If it's our own message and wasn't found by chatId, match with the optimistic entry
                    if (!alreadyExists && currentUserId != null && currentUserId.equals(msg.senderId)) {
                        for (ChatMessageResponse existing : messages) {
                            if (existing.chatId == null && msg.message.equals(existing.message)) {
                                existing.chatId = msg.chatId;
                                existing.sentAt = msg.sentAt;
                                existing.isRead = msg.isRead;
                                existing.read = msg.read;
                                alreadyExists = true;
                                break;
                            }
                        }
                    }

                    if (!alreadyExists) {
                        messages.add(msg);
                        addMessageBubble(msg);
                        scrollToBottom();

                        // 3. Mark the message as read if it is from the opponent
                        if (currentUserId != null && !currentUserId.equals(msg.senderId)) {
                            markMessagesAsRead();
                        }
                    }
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

    private void setChatAvatarInitial(String name) {
        if (txtChatAvatarInitial == null) {
            return;
        }

        if (name == null || name.trim().isEmpty()) {
            txtChatAvatarInitial.setText("?");
            return;
        }

        String cleanName = name.trim();
        txtChatAvatarInitial.setText(cleanName.substring(0, 1).toUpperCase());
    }


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
                                && response.body().success) {
                            messages.clear();
                            if (response.body().data != null) {
                                messages.addAll(response.body().data);
                            }
                            renderMessages();
                        } else {
                            String err = "Gagal memuat riwayat chat";
                            if (response.code() != 200) {
                                err += " (HTTP " + response.code() + ")";
                            }
                            AppToast.error(ChatActivity.this, err);
                        }
                        // Mark messages as read
                        markMessagesAsRead();
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<ChatMessageResponse>>> call, Throwable t) {
                        loadingHistory = false;
                        AppToast.error(ChatActivity.this, "Koneksi gagal: Gagal memuat riwayat chat.");
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

    private void updateSendButtonState(boolean hasText) {
        if (btnSend == null) {
            return;
        }

        btnSend.setEnabled(true);
        btnSend.setAlpha(1.0f);

        if (hasText) {
            btnSend.setBackgroundResource(R.drawable.bg_chat_send_circle_active);
            btnSend.setTextColor(Color.WHITE);
        } else {
            btnSend.setBackgroundResource(R.drawable.bg_chat_send_circle_inactive);
            btnSend.setTextColor(Color.parseColor("#6B7280"));
        }
    }

    private void sendMessage() {
        String message = edtMessage.getText().toString().trim();
        if (message.isEmpty()) return;

        edtMessage.setText("");

        // Optimistically add own message to UI immediately
        ChatMessageResponse optimisticMsg = new ChatMessageResponse();
        optimisticMsg.chatId = null;
        optimisticMsg.serviceRequestId = serviceRequestId;
        optimisticMsg.senderId = currentUserId;
        optimisticMsg.senderName = null; // own message, no name needed
        optimisticMsg.message = message;
        optimisticMsg.sentAt = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", java.util.Locale.US).format(new java.util.Date());
        optimisticMsg.isRead = false;
        optimisticMsg.read = false;

        messages.add(optimisticMsg);
        renderMessages();

        sendMessageViaWebSocket(message);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UI rendering
    // ─────────────────────────────────────────────────────────────────────────

    private void renderMessages() {
        layoutMessages.removeAllViews();

        boolean hasRenderableMessage = false;
        boolean dateDividerAdded = false;

        for (ChatMessageResponse msg : messages) {
            if (msg == null || isBlank(msg.message)) {
                continue;
            }

            if (!dateDividerAdded) {
                addDateDivider("Hari ini");
                dateDividerAdded = true;
            }

            hasRenderableMessage = true;
            addMessageBubble(msg);
        }

        if (txtChatEmptyState != null) {
            txtChatEmptyState.setVisibility(hasRenderableMessage ? View.GONE : View.VISIBLE);
        }

        if (hasRenderableMessage) {
            scrollToBottom();
        }
    }

    private void addDateDivider(String label) {
        if (layoutMessages == null || isBlank(label)) {
            return;
        }

        TextView dateText = new TextView(this);
        dateText.setText(label);
        dateText.setTextColor(Color.parseColor("#60737A"));
        dateText.setTextSize(11);
        dateText.setTypeface(Typeface.DEFAULT_BOLD);
        dateText.setGravity(Gravity.CENTER);
        dateText.setIncludeFontPadding(false);
        dateText.setBackgroundResource(R.drawable.bg_chat_date_chip);
        dateText.setPadding(dp(12), dp(6), dp(12), dp(6));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.gravity = Gravity.CENTER_HORIZONTAL;
        params.setMargins(0, dp(2), 0, dp(14));
        dateText.setLayoutParams(params);

        layoutMessages.addView(dateText);
    }

    private void addMessageBubble(ChatMessageResponse msg) {
        if (msg == null || isBlank(msg.message)) {
            return;
        }

        if (txtChatEmptyState != null) {
            txtChatEmptyState.setVisibility(View.GONE);
        }

        String normalizedMessage = msg.message.trim();
        boolean isMine = currentUserId != null && currentUserId.equals(msg.senderId);

        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int maxBubbleWidth = (int) (screenWidth * 0.72f);
        int maxTextWidth = maxBubbleWidth - dp(26);

        boolean isLongMessage = normalizedMessage.length() >= 34
                || normalizedMessage.contains("\n")
                || normalizedMessage.contains("/")
                || normalizedMessage.contains("{")
                || normalizedMessage.contains("}");

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(isMine ? Gravity.END : Gravity.START);

        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        rowParams.setMargins(0, dp(2), 0, dp(4));
        row.setLayoutParams(rowParams);

        LinearLayout bubble = new LinearLayout(this);
        bubble.setOrientation(LinearLayout.VERTICAL);
        bubble.setBackgroundResource(isMine
                ? R.drawable.bg_chat_bubble_mine
                : R.drawable.bg_chat_bubble_theirs);
        bubble.setPadding(dp(12), dp(8), dp(12), dp(6));
        bubble.setMinimumWidth(isLongMessage ? maxBubbleWidth : dp(82));

        LinearLayout.LayoutParams bubbleParams = new LinearLayout.LayoutParams(
                isLongMessage ? maxBubbleWidth : ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        bubbleParams.setMargins(
                isMine ? dp(70) : dp(2),
                0,
                isMine ? dp(2) : dp(70),
                0
        );
        bubble.setLayoutParams(bubbleParams);

        TextView messageText = new TextView(this);
        messageText.setText(normalizedMessage);
        messageText.setTextColor(isMine ? Color.WHITE : Color.parseColor("#1F2329"));
        messageText.setTextSize(15);
        messageText.setLineSpacing(dp(2), 1.0f);
        messageText.setIncludeFontPadding(false);
        messageText.setSingleLine(false);
        messageText.setHorizontallyScrolling(false);
        messageText.setMaxWidth(maxTextWidth);
        messageText.setMinWidth(dp(28));

        LinearLayout.LayoutParams messageParams = new LinearLayout.LayoutParams(
                isLongMessage ? ViewGroup.LayoutParams.MATCH_PARENT : ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        messageText.setLayoutParams(messageParams);

        bubble.addView(messageText);

        String time = formatTime(msg.sentAt);
        String metaText = buildMessageMetaText(msg, isMine, time);

        if (!isBlank(metaText)) {
            TextView timeText = new TextView(this);
            timeText.setText(metaText);
            timeText.setTextColor(getMessageMetaColor(msg, isMine));
            timeText.setTextSize(10);
            timeText.setIncludeFontPadding(false);

            LinearLayout.LayoutParams timeParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            timeParams.setMargins(dp(18), dp(5), 0, 0);
            timeParams.gravity = Gravity.END;
            timeText.setLayoutParams(timeParams);

            bubble.addView(timeText);
        }

        row.addView(bubble);
        layoutMessages.addView(row);
    }

    private String buildMessageMetaText(ChatMessageResponse msg, boolean isMine, String time) {
        if (!isMine) {
            return time;
        }

        String ticks;

        if (msg != null && msg.isReadFromBackend()) {
            // DB/backend bilang sudah dibaca.
            ticks = "✓✓";
        } else {
            // Belum dibaca atau belum ada status read dari backend.
            ticks = "✓";
        }

        if (isBlank(time)) {
            return ticks;
        }

        return time + "  " + ticks;
    }

    private int getMessageMetaColor(ChatMessageResponse msg, boolean isMine) {
        if (!isMine) {
            return Color.parseColor("#92A0A6");
        }

        if (msg != null && msg.isReadFromBackend()) {
            // Biru hanya kalau backend/DB bilang read.
            return Color.parseColor("#4FC3F7");
        }

        // Belum read: abu-abu.
        return Color.parseColor("#B8C7D9");
    }

    private void scrollToBottom() {
        scrollMessages.post(() -> scrollMessages.fullScroll(View.FOCUS_DOWN));
        scrollMessages.postDelayed(() -> scrollMessages.fullScroll(View.FOCUS_DOWN), 100);
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
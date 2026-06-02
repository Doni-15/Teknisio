package com.teknisio.mobile.view.customer;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.teknisio.mobile.R;
import com.teknisio.mobile.base.BaseActivity;
import com.teknisio.mobile.model.response.NotificationResponse;
import com.teknisio.mobile.util.BackButtonHelper;

import java.util.ArrayList;
import java.util.List;

public class NotificationActivity extends BaseActivity {

    private FrameLayout btnBack;
    private TextView txtNotificationSubtitle;
    private LinearLayout layoutNotifications;
    private TextView txtNotificationEmpty;
    private Button btnRetry;

    private final List<NotificationResponse> notifications = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        bindViews();
        setupActions();
        loadDummyNotifications();
    }

    private void bindViews() {
        btnBack = findViewById(R.id.btnBack);
        txtNotificationSubtitle = findViewById(R.id.txtNotificationSubtitle);
        layoutNotifications = findViewById(R.id.layoutNotifications);
        txtNotificationEmpty = findViewById(R.id.txtNotificationEmpty);
        btnRetry = findViewById(R.id.btnRetry);
    }

    private void setupActions() {
        BackButtonHelper.setup(btnBack, this::finish);
        btnRetry.setOnClickListener(v -> loadDummyNotifications());
    }

    private void loadDummyNotifications() {
        notifications.clear();

        notifications.add(createDummyNotification(
                "dummy-order-waiting",
                "Pesanan menunggu teknisi",
                "Request servis kamu sudah dibuat dan sedang menunggu respons teknisi.",
                "Simulasi UI"
        ));

        notifications.add(createDummyNotification(
                "dummy-technician-update",
                "Update dari teknisi",
                "Teknisi akan menghubungi kamu melalui nomor telepon yang terdaftar.",
                "Simulasi UI"
        ));

        notifications.add(createDummyNotification(
                "dummy-feature-info",
                "Notifikasi real-time segera hadir",
                "Fitur notifikasi backend akan diaktifkan pada versi berikutnya.",
                "Roadmap"
        ));

        renderNotifications();
    }

    private NotificationResponse createDummyNotification(
            String id,
            String title,
            String message,
            String time
    ) {
        NotificationResponse notification = new NotificationResponse();
        notification.id = id;
        notification.title = title;
        notification.message = message;
        notification.createdAt = time;
        notification.isRead = false;
        notification.read = false;
        return notification;
    }

    private void renderNotifications() {
        txtNotificationEmpty.setVisibility(android.view.View.GONE);
        layoutNotifications.removeAllViews();

        int unread = 0;

        for (NotificationResponse notification : notifications) {
            if (notification != null && notification.isUnread()) {
                unread++;
            }

            layoutNotifications.addView(createNotificationCard(notification));
        }

        txtNotificationSubtitle.setText(
                unread > 0
                        ? unread + " notifikasi belum dibaca"
                        : notifications.size() + " notifikasi"
        );

        btnRetry.setText("Muat ulang dummy");
        btnRetry.setEnabled(true);
    }

    private LinearLayout createNotificationCard(NotificationResponse notification) {
        boolean unread = notification != null && notification.isUnread();

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.TOP);
        card.setPadding(dp(14), dp(14), dp(14), dp(14));
        card.setBackground(makeStrokeRounded(
                "#FFFFFF",
                unread ? "#2F4A8A" : "#DCE6EB",
                18,
                unread ? 2 : 1
        ));
        card.setElevation(dp(unread ? 4 : 1));

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, dp(12));
        card.setLayoutParams(cardParams);

        TextView icon = new TextView(this);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(44), dp(44));
        icon.setLayoutParams(iconParams);
        icon.setGravity(Gravity.CENTER);
        icon.setBackground(makeOval(unread ? "#EAF4FF" : "#F3F7F9"));
        icon.setText("!");
        icon.setTextColor(Color.parseColor("#2F4A8A"));
        icon.setTextSize(20);
        icon.setTypeface(Typeface.DEFAULT_BOLD);

        LinearLayout content = new LinearLayout(this);
        LinearLayout.LayoutParams contentParams = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
        );
        contentParams.setMargins(dp(12), 0, 0, 0);
        content.setLayoutParams(contentParams);
        content.setOrientation(LinearLayout.VERTICAL);

        TextView title = new TextView(this);
        title.setText(notification == null ? "Notifikasi Teknisio" : notification.getDisplayTitle());
        title.setTextColor(Color.parseColor("#1F2329"));
        title.setTextSize(16);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setMaxLines(2);
        title.setEllipsize(TextUtils.TruncateAt.END);

        TextView message = new TextView(this);
        LinearLayout.LayoutParams messageParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        messageParams.setMargins(0, dp(5), 0, 0);
        message.setLayoutParams(messageParams);
        message.setText(notification == null ? "Ada pembaruan dari sistem." : notification.getDisplayMessage());
        message.setTextColor(Color.parseColor("#5F6B73"));
        message.setTextSize(13);
        message.setMaxLines(3);
        message.setEllipsize(TextUtils.TruncateAt.END);

        TextView time = new TextView(this);
        LinearLayout.LayoutParams timeParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        timeParams.setMargins(0, dp(8), 0, 0);
        time.setLayoutParams(timeParams);
        time.setText(notification == null ? "" : notification.getDisplayTime());
        time.setTextColor(Color.parseColor("#8A949B"));
        time.setTextSize(11);
        time.setSingleLine(true);
        time.setEllipsize(TextUtils.TruncateAt.END);

        content.addView(title);
        content.addView(message);

        if (notification != null && !isBlank(notification.getDisplayTime())) {
            content.addView(time);
        }

        card.addView(icon);
        card.addView(content);

        card.setOnClickListener(v -> markAsReadLocal(notification));

        return card;
    }

    private void markAsReadLocal(NotificationResponse notification) {
        if (notification == null || !notification.isUnread()) {
            return;
        }

        notification.markReadLocal();
        renderNotifications();
    }

    private GradientDrawable makeStrokeRounded(String fillColor, String strokeColor, int radiusDp, int strokeWidthDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Color.parseColor(fillColor));
        drawable.setCornerRadius(dp(radiusDp));
        drawable.setStroke(dp(strokeWidthDp), Color.parseColor(strokeColor));
        return drawable;
    }

    private GradientDrawable makeOval(String color) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setColor(Color.parseColor(color));
        return drawable;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}

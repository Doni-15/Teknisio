package com.teknisio.mobile.view.technician;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.teknisio.mobile.R;
import com.teknisio.mobile.base.BaseActivity;
import com.teknisio.mobile.local.TokenManager;
import com.teknisio.mobile.model.response.ApiResponse;
import com.teknisio.mobile.model.response.ServiceRequestResponse;
import com.teknisio.mobile.network.ApiClient;
import com.teknisio.mobile.util.AppToast;
import com.teknisio.mobile.util.ErrorParser;
import com.teknisio.mobile.util.OrderStatusHelper;
import com.teknisio.mobile.util.TextHelper;
import com.teknisio.mobile.util.ViewHelper;
import com.teknisio.mobile.view.auth.LoginActivity;
import com.teknisio.mobile.view.customer.NotificationActivity;
import com.teknisio.mobile.view.customer.AccountActivity;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TechnicianHomeActivity extends BaseActivity {


    private TextView txtTechnicianAvatar;
    private TextView txtTechnicianGreeting;
    private TextView txtTechnicianSubtitle;
    private TextView txtTechnicianRequestCount;
    private TextView btnTechnicianLogout;
    private FrameLayout btnTechnicianNotification;
    private LinearLayout layoutTechnicianRequests;
    private TextView txtTechnicianEmpty;
    private Button btnRefreshTechnicianRequests;

    private LinearLayout navHome;
    private LinearLayout navChat;
    private LinearLayout navHistory;
    private LinearLayout navAccount;

    private TokenManager tokenManager;
    private final List<ServiceRequestResponse> requests = new ArrayList<>();
    private boolean loading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_technician_home);

        tokenManager = new TokenManager(this);

        bindViews();
        setupHeader();
        setupActions();
        loadRequests();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (layoutTechnicianRequests != null) {
            loadRequests();
        }
    }

    private void bindViews() {
        txtTechnicianAvatar = findViewById(R.id.txtTechnicianAvatar);
        txtTechnicianGreeting = findViewById(R.id.txtTechnicianGreeting);
        txtTechnicianSubtitle = findViewById(R.id.txtTechnicianSubtitle);
        txtTechnicianRequestCount = findViewById(R.id.txtTechnicianRequestCount);
        btnTechnicianLogout = findViewById(R.id.btnTechnicianLogout);
        btnTechnicianNotification = findViewById(R.id.btnTechnicianNotification);
        layoutTechnicianRequests = findViewById(R.id.layoutTechnicianRequests);
        txtTechnicianEmpty = findViewById(R.id.txtTechnicianEmpty);
        btnRefreshTechnicianRequests = findViewById(R.id.btnRefreshTechnicianRequests);

        navHome = findViewById(R.id.navHome);
        navChat = findViewById(R.id.navChat);
        navHistory = findViewById(R.id.navHistory);
        navAccount = findViewById(R.id.navAccount);
    }

    private void setupHeader() {
        String name = tokenManager.getName();
        String address = tokenManager.getAddress();

        txtTechnicianAvatar.setText(getInitial(name));
        txtTechnicianGreeting.setText(getSafeText(name, "Teknisi"));
        txtTechnicianSubtitle.setText(getSafeText(address, "Alamat belum diatur"));
        setRequestCountText("Memuat request...");
    }

    private void setupActions() {
        if (btnRefreshTechnicianRequests != null) {
            btnRefreshTechnicianRequests.setOnClickListener(v -> loadRequests());
        }

        if (btnTechnicianNotification != null) {
            btnTechnicianNotification.setOnClickListener(v -> {
                Intent intent = new Intent(TechnicianHomeActivity.this, NotificationActivity.class);
                startActivity(intent);
            });
        }

        if (navHome != null) {
            navHome.setOnClickListener(v -> loadRequests());
        }

        if (navChat != null) {
            navChat.setOnClickListener(v -> Toast.makeText(this, "Chat teknisi belum tersedia.", Toast.LENGTH_SHORT).show());
        }

        if (navHistory != null) {
            navHistory.setOnClickListener(v -> {
                Intent intent = new Intent(TechnicianHomeActivity.this, TechnicianHistoryActivity.class);
                startActivity(intent);
            });
        }

        if (navAccount != null) {
            navAccount.setOnClickListener(v -> {
                Intent intent = new Intent(TechnicianHomeActivity.this, AccountActivity.class);
                startActivity(intent);
            });
            navAccount.setOnLongClickListener(v -> {
                logout();
                return true;
            });
        }

        if (btnTechnicianLogout != null) {
            btnTechnicianLogout.setOnClickListener(v -> logout());
        }
    }

    private void logout() {
        tokenManager.clearSession();
        ApiClient.reset();

        Intent intent = new Intent(TechnicianHomeActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void loadRequests() {
        if (loading) {
            return;
        }

        loading = true;
        showMessage("Memuat request...");

        ApiClient.getApiService(this)
                .getTechnicianServiceRequests(null, "latest")
                .enqueue(new Callback<ApiResponse<List<ServiceRequestResponse>>>() {
                    @Override
                    public void onResponse(
                            Call<ApiResponse<List<ServiceRequestResponse>>> call,
                            Response<ApiResponse<List<ServiceRequestResponse>>> response
                    ) {
                        loading = false;

                        if (!response.isSuccessful()) {
                            showMessage(ErrorParser.parseError(response, "Request gagal dimuat."));
                            return;
                        }

                        ApiResponse<List<ServiceRequestResponse>> body = response.body();

                        if (body == null || !body.success) {
                            showMessage(ErrorParser.getBestMessage(body, "Request gagal dimuat."));
                            return;
                        }

                        requests.clear();

                        if (body.data != null) {
                            for (ServiceRequestResponse request : body.data) {
                                if (isHomeVisibleStatus(request == null ? null : request.status)) {
                                    requests.add(request);
                                }
                            }
                        }

                        renderRequests();
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<ServiceRequestResponse>>> call, Throwable t) {
                        loading = false;
                        showMessage("Tidak bisa terhubung ke server.");
                    }
                });
    }

    private void renderRequests() {
        layoutTechnicianRequests.removeAllViews();

        if (requests.isEmpty()) {
            showMessage("Belum ada request aktif.");
            setRequestCountText("0 request aktif");
            return;
        }

        txtTechnicianEmpty.setVisibility(android.view.View.GONE);
        setRequestCountText(requests.size() + " request aktif");

        for (ServiceRequestResponse request : requests) {
            layoutTechnicianRequests.addView(createRequestCard(request));
        }

        btnRefreshTechnicianRequests.setText("Refresh");
        btnRefreshTechnicianRequests.setEnabled(true);
    }

    private LinearLayout createRequestCard(ServiceRequestResponse request) {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(2), dp(12), dp(2), dp(12));

        TextView avatar = new TextView(this);
        LinearLayout.LayoutParams avatarParams = new LinearLayout.LayoutParams(dp(54), dp(54));
        avatar.setLayoutParams(avatarParams);
        avatar.setGravity(Gravity.CENTER);
        avatar.setBackground(makeRounded("#D7F4F8", 27));
        avatar.setText(getInitial(request == null ? null : request.customerName));
        avatar.setTextColor(Color.parseColor("#2F4A8A"));
        avatar.setTextSize(16);
        avatar.setTypeface(Typeface.DEFAULT_BOLD);

        LinearLayout content = new LinearLayout(this);
        LinearLayout.LayoutParams contentParams = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
        );
        contentParams.setMargins(dp(16), 0, dp(8), 0);
        content.setLayoutParams(contentParams);
        content.setOrientation(LinearLayout.VERTICAL);

        TextView name = new TextView(this);
        name.setText(getSafeText(request == null ? null : request.customerName, "Pelanggan"));
        name.setTextColor(Color.parseColor("#1F2329"));
        name.setTextSize(17);
        name.setTypeface(Typeface.DEFAULT_BOLD);
        name.setSingleLine(true);
        name.setEllipsize(TextUtils.TruncateAt.END);

        LinearLayout infoRow = new LinearLayout(this);
        LinearLayout.LayoutParams infoRowParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        infoRowParams.setMargins(0, dp(8), 0, 0);
        infoRow.setLayoutParams(infoRowParams);
        infoRow.setGravity(Gravity.CENTER_VERTICAL);
        infoRow.setOrientation(LinearLayout.HORIZONTAL);

        TextView icon = new TextView(this);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(30), dp(30));
        icon.setLayoutParams(iconParams);
        icon.setGravity(Gravity.CENTER);
        icon.setBackground(makeRounded("#E2F7FA", 15));
        icon.setText("✓");
        icon.setTextColor(Color.parseColor("#2F4A8A"));
        icon.setTextSize(13);
        icon.setTypeface(Typeface.DEFAULT_BOLD);

        TextView category = new TextView(this);
        LinearLayout.LayoutParams categoryParams = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
        );
        categoryParams.setMargins(dp(9), 0, 0, 0);
        category.setLayoutParams(categoryParams);
        category.setText(getCategoriesText(request));
        category.setTextColor(Color.parseColor("#1F2329"));
        category.setTextSize(14);
        category.setSingleLine(true);
        category.setEllipsize(TextUtils.TruncateAt.END);

        infoRow.addView(icon);
        infoRow.addView(category);

        content.addView(name);
        content.addView(infoRow);

        ImageView arrow = new ImageView(this);
        LinearLayout.LayoutParams arrowParams = new LinearLayout.LayoutParams(dp(28), dp(28));
        arrow.setLayoutParams(arrowParams);
        arrow.setImageResource(R.drawable.ic_chevron_right);

        row.addView(avatar);
        row.addView(content);
        row.addView(arrow);

        View divider = new View(this);
        LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                1
        );
        dividerParams.setMargins(dp(8), 0, dp(8), 0);
        divider.setLayoutParams(dividerParams);
        divider.setBackgroundColor(Color.parseColor("#DCE6EB"));

        container.addView(row);
        container.addView(divider);

        container.setOnClickListener(v -> openRequestDetail(request));

        return container;
    }

    private void openRequestDetail(ServiceRequestResponse request) {
        if (request == null || isBlank(request.serviceRequestId)) {
            AppToast.error(this, "Data request tidak valid.");
            return;
        }

        Intent intent = new Intent(TechnicianHomeActivity.this, TechnicianRequestDetailActivity.class);
        intent.putExtra(TechnicianRequestDetailActivity.EXTRA_SERVICE_REQUEST_ID, request.serviceRequestId);
        startActivity(intent);
    }

    private void setRequestCountText(String text) {
        if (txtTechnicianRequestCount != null) {
            txtTechnicianRequestCount.setText(getSafeText(text, "0 request"));
        }
    }

    private void showMessage(String message) {
        layoutTechnicianRequests.removeAllViews();
        txtTechnicianEmpty.setVisibility(android.view.View.VISIBLE);
        txtTechnicianEmpty.setText(message);
        setRequestCountText(message);
        btnRefreshTechnicianRequests.setText(loading ? "Memuat..." : "Refresh");
        btnRefreshTechnicianRequests.setEnabled(!loading);
    }

    private String getCategoriesText(ServiceRequestResponse request) {
        return TextHelper.deviceCategoriesText(
                request == null ? null : request.selectedDeviceCategories,
                "Kategori perangkat tidak tersedia",
                ", "
        );
    }

    private boolean isHomeVisibleStatus(String status) {
        String normalized = OrderStatusHelper.normalize(status);

        return "WAITING".equals(normalized)
                || "ACCEPTED".equals(normalized)
                || "ON_PROGRESS".equals(normalized);
    }


    private String getInitial(String value) {
        return TextHelper.initial(value, "T");
    }

    private String getSafeText(String value, String fallback) {
        return TextHelper.safe(value, fallback);
    }

    private boolean isBlank(String value) {
        return TextHelper.isBlank(value);
    }

    private GradientDrawable makeRounded(String color, int radiusDp) {
        return ViewHelper.rounded(this, color, radiusDp);
    }

    private GradientDrawable makeStrokeRounded(String fillColor, String strokeColor, int radiusDp, int strokeWidthDp) {
        return ViewHelper.strokeRounded(this, fillColor, strokeColor, radiusDp, strokeWidthDp);
    }

    private int dp(int value) {
        return ViewHelper.dp(this, value);
    }
}

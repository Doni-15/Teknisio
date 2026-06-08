package com.teknisio.mobile.view.customer;

import android.content.Intent;
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
import android.widget.Toast;

import com.teknisio.mobile.R;
import com.teknisio.mobile.base.BaseActivity;
import com.teknisio.mobile.model.response.ApiResponse;
import com.teknisio.mobile.model.response.DeviceCategoryResponse;
import com.teknisio.mobile.model.response.ServiceRequestResponse;
import com.teknisio.mobile.network.ApiClient;
import com.teknisio.mobile.util.BackButtonHelper;
import com.teknisio.mobile.util.ErrorParser;
import com.teknisio.mobile.util.OrderStatusHelper;
import com.teknisio.mobile.util.TextHelper;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderHistoryActivity extends BaseActivity {

    private static final String[] FILTER_LABELS = {
            "All", "Waiting", "Accepted", "On Progress", "Completed", "Cancelled", "Rejected"
    };

    private static final String[] FILTER_VALUES = {
            null, "WAITING", "ACCEPTED", "ON_PROGRESS", "COMPLETED", "CANCELLED", "REJECTED"
    };

    private FrameLayout btnBack;
    private TextView txtHistorySubtitle;
    private LinearLayout layoutStatusFilters;
    private LinearLayout layoutOrders;
    private TextView txtOrderEmpty;
    private Button btnRefreshOrders;

    private final List<ServiceRequestResponse> orders = new ArrayList<>();
    private String selectedStatus = null;
    private boolean loading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        bindViews();
        setupActions();
        renderFilters();
        loadOrders();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (layoutOrders != null) {
            loadOrders();
        }
    }

    private void bindViews() {
        btnBack = findViewById(R.id.btnBack);
        txtHistorySubtitle = findViewById(R.id.txtHistorySubtitle);
        layoutStatusFilters = findViewById(R.id.layoutStatusFilters);
        layoutOrders = findViewById(R.id.layoutOrders);
        txtOrderEmpty = findViewById(R.id.txtOrderEmpty);
        btnRefreshOrders = findViewById(R.id.btnRefreshOrders);
    }

    private void setupActions() {
        BackButtonHelper.setup(btnBack, this::finish);
        btnRefreshOrders.setOnClickListener(v -> loadOrders());
    }

    private void renderFilters() {
        layoutStatusFilters.removeAllViews();

        for (int i = 0; i < FILTER_LABELS.length; i++) {
            final String value = FILTER_VALUES[i];
            boolean selected = isSameStatus(selectedStatus, value);

            TextView chip = new TextView(this);
            chip.setText(FILTER_LABELS[i]);
            chip.setTextSize(13);
            chip.setTypeface(Typeface.DEFAULT_BOLD);
            chip.setGravity(Gravity.CENTER);
            chip.setTextColor(selected ? Color.WHITE : Color.parseColor("#2F4A8A"));
            chip.setPadding(dp(14), dp(8), dp(14), dp(8));
            chip.setBackground(makeStrokeRounded(
                    selected ? "#2F4A8A" : "#FFFFFF",
                    "#DCE6EB",
                    18,
                    1
            ));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    dp(38)
            );
            params.setMargins(0, 0, dp(8), 0);
            chip.setLayoutParams(params);

            chip.setOnClickListener(v -> {
                selectedStatus = value;
                renderFilters();
                loadOrders();
            });

            layoutStatusFilters.addView(chip);
        }
    }

    private void loadOrders() {
        if (loading) {
            return;
        }

        loading = true;
        showMessage("Memuat order...");

        ApiClient.getApiService(this)
                .getMyServiceRequests(selectedStatus)
                .enqueue(new Callback<ApiResponse<List<ServiceRequestResponse>>>() {
                    @Override
                    public void onResponse(
                            Call<ApiResponse<List<ServiceRequestResponse>>> call,
                            Response<ApiResponse<List<ServiceRequestResponse>>> response
                    ) {
                        loading = false;

                        if (!response.isSuccessful()) {
                            showMessage(ErrorParser.parseError(response, "Order gagal dimuat."));
                            return;
                        }

                        ApiResponse<List<ServiceRequestResponse>> body = response.body();

                        if (body == null || !body.success) {
                            showMessage(ErrorParser.getBestMessage(body, "Order gagal dimuat."));
                            return;
                        }

                        orders.clear();

                        if (body.data != null) {
                            orders.addAll(body.data);
                        }

                        renderOrders();
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<ServiceRequestResponse>>> call, Throwable t) {
                        loading = false;
                        showMessage("Tidak bisa terhubung ke server.");
                    }
                });
    }

    private void renderOrders() {
        layoutOrders.removeAllViews();

        if (orders.isEmpty()) {
            showMessage("Belum ada order" + getFilterSuffix() + ".");
            txtHistorySubtitle.setText("0 order");
            return;
        }

        txtOrderEmpty.setVisibility(android.view.View.GONE);
        txtHistorySubtitle.setText(orders.size() + " order" + getFilterSuffix());

        for (ServiceRequestResponse order : orders) {
            layoutOrders.addView(createOrderCard(order));
        }

        btnRefreshOrders.setText("Refresh");
        btnRefreshOrders.setEnabled(true);
    }

    private LinearLayout createOrderCard(ServiceRequestResponse order) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(16), dp(16), dp(16));
        card.setBackground(makeStrokeRounded("#FFFFFF", "#DCE6EB", 18, 1));
        card.setElevation(dp(2));

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, dp(12));
        card.setLayoutParams(cardParams);

        LinearLayout topRow = new LinearLayout(this);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView code = new TextView(this);
        code.setText(getOrderCode(order));
        code.setTextColor(Color.parseColor("#1F2329"));
        code.setTextSize(17);
        code.setTypeface(Typeface.DEFAULT_BOLD);
        code.setSingleLine(true);
        code.setEllipsize(TextUtils.TruncateAt.END);

        LinearLayout.LayoutParams codeParams = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
        );
        code.setLayoutParams(codeParams);

        TextView status = new TextView(this);
        status.setText(OrderStatusHelper.getDisplayStatus(order == null ? null : order.status));
        status.setTextColor(Color.WHITE);
        status.setTextSize(12);
        status.setTypeface(Typeface.DEFAULT_BOLD);
        status.setGravity(Gravity.CENTER);
        status.setPadding(dp(10), dp(5), dp(10), dp(5));
        status.setBackground(makeRounded(
                OrderStatusHelper.getStatusColor(order == null ? null : order.status),
                16
        ));

        topRow.addView(code);
        topRow.addView(status);

        TextView categories = new TextView(this);
        categories.setText(getCategoriesText(order));
        categories.setTextColor(Color.parseColor("#2F4A8A"));
        categories.setTextSize(13);
        categories.setTypeface(Typeface.DEFAULT_BOLD);
        categories.setMaxLines(1);
        categories.setEllipsize(TextUtils.TruncateAt.END);
        categories.setPadding(0, dp(10), 0, 0);

        TextView issue = new TextView(this);
        issue.setText(getSafeText(order == null ? null : order.issueDescription, "Deskripsi masalah tidak tersedia."));
        issue.setTextColor(Color.parseColor("#5F6B73"));
        issue.setTextSize(13);
        issue.setMaxLines(2);
        issue.setEllipsize(TextUtils.TruncateAt.END);
        issue.setPadding(0, dp(6), 0, 0);

        TextView time = new TextView(this);
        time.setText(TextHelper.formatDateTime(order == null ? null : order.requestTime));
        time.setTextColor(Color.parseColor("#8A949B"));
        time.setTextSize(11);
        time.setSingleLine(true);
        time.setEllipsize(TextUtils.TruncateAt.END);
        time.setPadding(0, dp(8), 0, 0);

        card.addView(topRow);
        card.addView(categories);
        card.addView(issue);

        if (!isBlank(order == null ? null : order.requestTime)) {
            card.addView(time);
        }

        card.setOnClickListener(v -> openOrderDetail(order));

        return card;
    }

    private void openOrderDetail(ServiceRequestResponse order) {
        if (order == null || isBlank(order.serviceRequestId)) {
            Toast.makeText(this, "Data order tidak valid.", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(OrderHistoryActivity.this, ServiceRequestDetailActivity.class);
        intent.putExtra(ServiceRequestDetailActivity.EXTRA_SERVICE_REQUEST_ID, order.serviceRequestId);
        startActivity(intent);
    }

    private void showMessage(String message) {
        layoutOrders.removeAllViews();
        txtOrderEmpty.setVisibility(android.view.View.VISIBLE);
        txtOrderEmpty.setText(message);
        txtHistorySubtitle.setText(message);
        btnRefreshOrders.setText(loading ? "Memuat..." : "Refresh");
        btnRefreshOrders.setEnabled(!loading);
    }

    private String getFilterSuffix() {
        return selectedStatus == null ? "" : " " + OrderStatusHelper.getDisplayStatus(selectedStatus);
    }

    private String getOrderCode(ServiceRequestResponse order) {
        if (order == null || isBlank(order.serviceRequestCode)) {
            return "Order";
        }

        return order.serviceRequestCode;
    }

    private String getCategoriesText(ServiceRequestResponse order) {
        if (order == null || order.selectedDeviceCategories == null || order.selectedDeviceCategories.isEmpty()) {
            return "Kategori perangkat tidak tersedia";
        }

        StringBuilder builder = new StringBuilder();

        for (DeviceCategoryResponse category : order.selectedDeviceCategories) {
            if (category == null || isBlank(category.name)) {
                continue;
            }

            if (builder.length() > 0) {
                builder.append(", ");
            }

            builder.append(category.name.trim());
        }

        return builder.length() == 0 ? "Kategori perangkat tidak tersedia" : builder.toString();
    }

    private boolean isSameStatus(String first, String second) {
        if (first == null && second == null) {
            return true;
        }

        if (first == null || second == null) {
            return false;
        }

        return first.equals(second);
    }

    private String getSafeText(String value, String fallback) {
        return isBlank(value) ? fallback : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private GradientDrawable makeRounded(String color, int radiusDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Color.parseColor(color));
        drawable.setCornerRadius(dp(radiusDp));
        return drawable;
    }

    private GradientDrawable makeStrokeRounded(String fillColor, String strokeColor, int radiusDp, int strokeWidthDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Color.parseColor(fillColor));
        drawable.setCornerRadius(dp(radiusDp));
        drawable.setStroke(dp(strokeWidthDp), Color.parseColor(strokeColor));
        return drawable;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}

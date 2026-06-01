package com.teknisio.mobile.view.technician;

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

import com.teknisio.mobile.R;
import com.teknisio.mobile.base.BaseActivity;
import com.teknisio.mobile.model.response.ApiResponse;
import com.teknisio.mobile.model.response.ServiceRequestResponse;
import com.teknisio.mobile.network.ApiClient;
import com.teknisio.mobile.util.AppToast;
import com.teknisio.mobile.util.BackButtonHelper;
import com.teknisio.mobile.util.ErrorParser;
import com.teknisio.mobile.util.OrderStatusHelper;
import com.teknisio.mobile.util.TextHelper;
import com.teknisio.mobile.util.ViewHelper;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TechnicianHistoryActivity extends BaseActivity {

    private static final String[] FILTER_LABELS = {
            "Semua", "Masuk", "Diterima", "Dikerjakan", "Selesai", "Dibatalkan", "Ditolak"
    };

    private static final String[] FILTER_VALUES = {
            null, "WAITING", "ACCEPTED", "ON_PROGRESS", "COMPLETED", "CANCELLED", "REJECTED"
    };

    private FrameLayout btnBack;
    private TextView txtTechnicianHistorySubtitle;
    private LinearLayout layoutTechnicianHistoryFilters;
    private LinearLayout layoutTechnicianHistoryRequests;
    private TextView txtTechnicianHistoryEmpty;
    private Button btnRefreshTechnicianHistory;

    private final List<ServiceRequestResponse> requests = new ArrayList<>();

    private String selectedStatus = null;
    private boolean loading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_technician_history);

        bindViews();
        setupActions();
        renderFilters();
        loadRequests();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (layoutTechnicianHistoryRequests != null) {
            loadRequests();
        }
    }

    private void bindViews() {
        btnBack = findViewById(R.id.btnBack);
        txtTechnicianHistorySubtitle = findViewById(R.id.txtTechnicianHistorySubtitle);
        layoutTechnicianHistoryFilters = findViewById(R.id.layoutTechnicianHistoryFilters);
        layoutTechnicianHistoryRequests = findViewById(R.id.layoutTechnicianHistoryRequests);
        txtTechnicianHistoryEmpty = findViewById(R.id.txtTechnicianHistoryEmpty);
        btnRefreshTechnicianHistory = findViewById(R.id.btnRefreshTechnicianHistory);
    }

    private void setupActions() {
        BackButtonHelper.setup(btnBack, this::finish);
        btnRefreshTechnicianHistory.setOnClickListener(v -> loadRequests());
    }

    private void renderFilters() {
        layoutTechnicianHistoryFilters.removeAllViews();

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
                loadRequests();
            });

            layoutTechnicianHistoryFilters.addView(chip);
        }
    }

    private void loadRequests() {
        if (loading) {
            return;
        }

        loading = true;
        showMessage("Memuat riwayat request...");

        ApiClient.getApiService(this)
                .getTechnicianServiceRequests(selectedStatus, "latest")
                .enqueue(new Callback<ApiResponse<List<ServiceRequestResponse>>>() {
                    @Override
                    public void onResponse(
                            Call<ApiResponse<List<ServiceRequestResponse>>> call,
                            Response<ApiResponse<List<ServiceRequestResponse>>> response
                    ) {
                        loading = false;

                        if (!response.isSuccessful()) {
                            showMessage(ErrorParser.parseError(response, "Riwayat request gagal dimuat."));
                            return;
                        }

                        ApiResponse<List<ServiceRequestResponse>> body = response.body();

                        if (body == null || !body.success) {
                            showMessage(ErrorParser.getBestMessage(body, "Riwayat request gagal dimuat."));
                            return;
                        }

                        requests.clear();

                        if (body.data != null) {
                            requests.addAll(body.data);
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
        layoutTechnicianHistoryRequests.removeAllViews();

        if (requests.isEmpty()) {
            showMessage("Belum ada riwayat request" + getFilterSuffix() + ".");
            txtTechnicianHistorySubtitle.setText("0 request");
            return;
        }

        txtTechnicianHistoryEmpty.setVisibility(android.view.View.GONE);
        txtTechnicianHistorySubtitle.setText(requests.size() + " request" + getFilterSuffix());

        for (ServiceRequestResponse request : requests) {
            layoutTechnicianHistoryRequests.addView(createRequestCard(request));
        }

        btnRefreshTechnicianHistory.setText("Refresh");
        btnRefreshTechnicianHistory.setEnabled(true);
    }

    private LinearLayout createRequestCard(ServiceRequestResponse request) {
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
        topRow.setGravity(Gravity.CENTER_VERTICAL);
        topRow.setOrientation(LinearLayout.HORIZONTAL);

        TextView code = new TextView(this);
        code.setText(TextHelper.safe(request == null ? null : request.serviceRequestCode, "Request"));
        code.setTextColor(Color.parseColor("#1F2329"));
        code.setTextSize(15);
        code.setTypeface(Typeface.DEFAULT_BOLD);
        code.setSingleLine(true);
        code.setEllipsize(TextUtils.TruncateAt.END);
        code.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView status = new TextView(this);
        status.setText(OrderStatusHelper.getDisplayStatus(request == null ? null : request.status));
        status.setTextColor(Color.WHITE);
        status.setTextSize(11);
        status.setTypeface(Typeface.DEFAULT_BOLD);
        status.setPadding(dp(10), dp(5), dp(10), dp(5));
        status.setBackground(makeRounded(
                OrderStatusHelper.getStatusColor(request == null ? null : request.status),
                14
        ));

        topRow.addView(code);
        topRow.addView(status);

        TextView customer = createText(
                "Pelanggan: " + TextHelper.safe(request == null ? null : request.customerName, "-"),
                "#1F2329",
                14,
                true
        );

        TextView categories = createText(
                getCategoriesText(request),
                "#6B7680",
                13,
                false
        );

        TextView issue = createText(
                TextHelper.safe(request == null ? null : request.issueDescription, "-"),
                "#6B7680",
                13,
                false
        );

        TextView time = createText(
                "Waktu request: " + TextHelper.safe(request == null ? null : request.requestTime, "-"),
                "#6B7680",
                12,
                false
        );

        card.addView(topRow);
        card.addView(customer);
        card.addView(categories);
        card.addView(issue);
        card.addView(time);

        card.setOnClickListener(v -> openRequestDetail(request));

        return card;
    }

    private TextView createText(String text, String color, int sizeSp, boolean bold) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextColor(Color.parseColor(color));
        view.setTextSize(sizeSp);
        view.setMaxLines(2);
        view.setEllipsize(TextUtils.TruncateAt.END);

        if (bold) {
            view.setTypeface(Typeface.DEFAULT_BOLD);
        }

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, dp(8), 0, 0);
        view.setLayoutParams(params);

        return view;
    }

    private void openRequestDetail(ServiceRequestResponse request) {
        if (request == null || TextHelper.isBlank(request.serviceRequestId)) {
            AppToast.error(this, "Data request tidak valid.");
            return;
        }

        Intent intent = new Intent(TechnicianHistoryActivity.this, TechnicianRequestDetailActivity.class);
        intent.putExtra(TechnicianRequestDetailActivity.EXTRA_SERVICE_REQUEST_ID, request.serviceRequestId);
        startActivity(intent);
    }

    private void showMessage(String message) {
        layoutTechnicianHistoryRequests.removeAllViews();
        txtTechnicianHistoryEmpty.setVisibility(android.view.View.VISIBLE);
        txtTechnicianHistoryEmpty.setText(message);
        txtTechnicianHistorySubtitle.setText(message);
        btnRefreshTechnicianHistory.setText(loading ? "Memuat..." : "Refresh");
        btnRefreshTechnicianHistory.setEnabled(!loading);
    }

    private String getCategoriesText(ServiceRequestResponse request) {
        return TextHelper.deviceCategoriesText(
                request == null ? null : request.selectedDeviceCategories,
                "Kategori perangkat tidak tersedia",
                ", "
        );
    }

    private String getFilterSuffix() {
        return selectedStatus == null ? "" : " " + OrderStatusHelper.getDisplayStatus(selectedStatus);
    }

    private boolean isSameStatus(String first, String second) {
        if (first == null && second == null) return true;
        if (first == null || second == null) return false;
        return first.equals(second);
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

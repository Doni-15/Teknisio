package com.teknisio.mobile.view.customer;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.teknisio.mobile.R;
import com.teknisio.mobile.base.BaseActivity;
import com.teknisio.mobile.model.request.CancelServiceRequestRequest;
import com.teknisio.mobile.model.response.ApiResponse;
import com.teknisio.mobile.model.response.DeviceCategoryResponse;
import com.teknisio.mobile.model.response.ServiceRequestResponse;
import com.teknisio.mobile.model.response.StatusHistoryResponse;
import com.teknisio.mobile.network.ApiClient;
import com.teknisio.mobile.util.AppToast;
import com.teknisio.mobile.util.BackButtonHelper;
import com.teknisio.mobile.util.ErrorParser;
import com.teknisio.mobile.util.OrderStatusHelper;
import com.teknisio.mobile.util.ReviewStateStore;
import com.teknisio.mobile.util.StatusHistoryRenderer;
import com.teknisio.mobile.view.customer.helper.ReviewDialogHelper;

import java.math.BigDecimal;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ServiceRequestDetailActivity extends BaseActivity {

    public static final String EXTRA_SERVICE_REQUEST_ID = "extra_service_request_id";

    private FrameLayout btnBack;
    private TextView txtOrderCode;
    private TextView txtOrderStatus;
    private TextView txtOrderTime;
    private TextView txtOrderCategories;
    private TextView txtOrderIssue;
    private TextView txtOrderAddress;
    private TextView txtCancelReason;
    private TextView txtDetailMessage;
    private Button btnCancelOrder;
    private Button btnWriteReview;
    private LinearLayout layoutStatusHistory;
    private TextView txtStatusHistoryLabel;

    private String serviceRequestId;
    private ServiceRequestResponse currentOrder;
    private boolean loading = false;
    private boolean cancelling = false;
    private boolean hasReview = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_request_detail);

        serviceRequestId = getIntent().getStringExtra(EXTRA_SERVICE_REQUEST_ID);

        bindViews();

        if (isBlank(serviceRequestId)) {
            AppToast.error(this, "Data order tidak valid.");
            finish();
            return;
        }

        setupActions();
        loadOrderDetail();
    }

    private void bindViews() {
        btnBack = findViewById(R.id.btnBack);
        txtOrderCode = findViewById(R.id.txtOrderCode);
        txtOrderStatus = findViewById(R.id.txtOrderStatus);
        txtOrderTime = findViewById(R.id.txtOrderTime);
        txtOrderCategories = findViewById(R.id.txtOrderCategories);
        txtOrderIssue = findViewById(R.id.txtOrderIssue);
        txtOrderAddress = findViewById(R.id.txtOrderAddress);
        txtCancelReason = findViewById(R.id.txtCancelReason);
        txtDetailMessage = findViewById(R.id.txtDetailMessage);
        btnCancelOrder = findViewById(R.id.btnCancelOrder);
        btnWriteReview = findViewById(R.id.btnWriteReview);
        layoutStatusHistory = findViewById(R.id.layoutStatusHistory);
        txtStatusHistoryLabel = findViewById(R.id.txtStatusHistoryLabel);
    }

    private void setupActions() {
        BackButtonHelper.setup(btnBack, this::finish);
        btnCancelOrder.setOnClickListener(v -> showCancelDialog());
        btnWriteReview.setOnClickListener(v -> showReviewDialog());
    }

    private void loadOrderDetail() {
        if (loading) {
            return;
        }

        loading = true;
        setMessage("Memuat detail order...");

        ApiClient.getApiService(this)
                .getMyServiceRequestDetail(serviceRequestId)
                .enqueue(new Callback<ApiResponse<ServiceRequestResponse>>() {
                    @Override
                    public void onResponse(
                            Call<ApiResponse<ServiceRequestResponse>> call,
                            Response<ApiResponse<ServiceRequestResponse>> response
                    ) {
                        loading = false;

                        if (!response.isSuccessful()) {
                            showError(ErrorParser.parseError(response, "Detail order gagal dimuat."));
                            return;
                        }

                        ApiResponse<ServiceRequestResponse> body = response.body();

                        if (body == null || !body.success || body.data == null) {
                            showError(ErrorParser.getBestMessage(body, "Detail order gagal dimuat."));
                            return;
                        }

                        currentOrder = body.data;
                        renderOrder(currentOrder);
                        loadStatusHistory();
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<ServiceRequestResponse>> call, Throwable t) {
                        loading = false;
                        showError("Tidak bisa terhubung ke server.");
                    }
                });
    }

    private void renderOrder(ServiceRequestResponse order) {
        txtOrderCode.setText(getSafeText(order.serviceRequestCode, "Order"));
        txtOrderStatus.setText(OrderStatusHelper.getDisplayStatus(order.status));
        txtOrderStatus.setBackground(makeRounded(OrderStatusHelper.getStatusColor(order.status), 16));
        txtOrderStatus.setTextColor(Color.WHITE);
        txtOrderStatus.setTypeface(Typeface.DEFAULT_BOLD);

        txtOrderTime.setText("Waktu request: " + getSafeText(order.requestTime, "-"));
        txtOrderCategories.setText(getCategoriesText(order));
        txtOrderIssue.setText(getSafeText(order.issueDescription, "-"));
        txtOrderAddress.setText(buildAddress(order));

        if (!isBlank(order.cancelReason)) {
            txtCancelReason.setVisibility(View.VISIBLE);
            txtCancelReason.setText("Alasan pembatalan:\n" + order.cancelReason.trim());
        } else if (!isBlank(order.rejectReason)) {
            txtCancelReason.setVisibility(View.VISIBLE);
            txtCancelReason.setText("Alasan penolakan:\n" + order.rejectReason.trim());
        } else {
            txtCancelReason.setVisibility(View.GONE);
            txtCancelReason.setText("");
        }

        if (OrderStatusHelper.canCancel(order.status)) {
            btnCancelOrder.setVisibility(View.VISIBLE);
            btnCancelOrder.setEnabled(!cancelling);
            btnCancelOrder.setAlpha(cancelling ? 0.65f : 1f);
        } else {
            btnCancelOrder.setVisibility(View.GONE);
        }

        hasReview = ReviewStateStore.isReviewed(this, serviceRequestId);

        // Show review button only for COMPLETED orders that have not been reviewed from this device.
        if ("COMPLETED".equals(OrderStatusHelper.normalize(order.status)) && !hasReview) {
            btnWriteReview.setVisibility(View.VISIBLE);
        } else {
            btnWriteReview.setVisibility(View.GONE);
        }

        txtDetailMessage.setText(getDetailMessage(order));
    }

    // -------------------------------------------------------------------------
    // Status History
    // -------------------------------------------------------------------------

    private void loadStatusHistory() {
        ApiClient.getApiService(this)
                .getMyServiceRequestStatusHistory(serviceRequestId)
                .enqueue(new Callback<ApiResponse<List<StatusHistoryResponse>>>() {
                    @Override
                    public void onResponse(
                            Call<ApiResponse<List<StatusHistoryResponse>>> call,
                            Response<ApiResponse<List<StatusHistoryResponse>>> response
                    ) {
                        if (!response.isSuccessful() || response.body() == null
                                || !response.body().success || response.body().data == null) return;
                        StatusHistoryRenderer.render(ServiceRequestDetailActivity.this, layoutStatusHistory, txtStatusHistoryLabel, response.body().data);
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<StatusHistoryResponse>>> call, Throwable t) {}
                });
    }

    // -------------------------------------------------------------------------
    // Review Dialog
    // -------------------------------------------------------------------------

    private void showReviewDialog() {
        ReviewDialogHelper.show(this, serviceRequestId, () -> {
            hasReview = true;
            btnWriteReview.setVisibility(View.GONE);
        });
    }

    // -------------------------------------------------------------------------
    // Cancel Dialog
    // -------------------------------------------------------------------------

    private void showCancelDialog() {
        if (currentOrder == null || !OrderStatusHelper.canCancel(currentOrder.status)) {
            AppToast.warning(this, "Order ini tidak bisa dibatalkan.");
            return;
        }

        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_cancel_order);
        dialog.setCanceledOnTouchOutside(true);

        EditText input = dialog.findViewById(R.id.edtCancelReason);
        TextView errorText = dialog.findViewById(R.id.txtCancelReasonError);
        Button btnKeepOrder = dialog.findViewById(R.id.btnKeepOrder);
        Button btnSubmitCancel = dialog.findViewById(R.id.btnSubmitCancel);

        if (btnKeepOrder != null) {
            btnKeepOrder.setOnClickListener(v -> dialog.dismiss());
        }

        if (btnSubmitCancel != null) {
            btnSubmitCancel.setOnClickListener(v -> {
                String reason = input == null || input.getText() == null
                        ? ""
                        : input.getText().toString().trim();

                if (reason.isEmpty()) {
                    if (errorText != null) {
                        errorText.setText("Alasan pembatalan wajib diisi.");
                        errorText.setVisibility(View.VISIBLE);
                    }

                    if (input != null) {
                        input.requestFocus();
                    }

                    return;
                }

                if (reason.length() > 1000) {
                    if (errorText != null) {
                        errorText.setText("Alasan pembatalan maksimal 1000 karakter.");
                        errorText.setVisibility(View.VISIBLE);
                    }

                    if (input != null) {
                        input.requestFocus();
                    }

                    return;
                }

                if (errorText != null) {
                    errorText.setVisibility(View.GONE);
                }

                cancelOrder(reason, dialog);
            });
        }

        dialog.show();

        Window window = dialog.getWindow();

        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(
                    getResources().getDisplayMetrics().widthPixels - dp(44),
                    WindowManager.LayoutParams.WRAP_CONTENT
            );
        }
    }

    private void cancelOrder(String reason, Dialog dialog) {
        if (cancelling || currentOrder == null || isBlank(currentOrder.serviceRequestId)) {
            return;
        }

        cancelling = true;
        btnCancelOrder.setEnabled(false);
        btnCancelOrder.setAlpha(0.65f);
        setMessage("Membatalkan order...");

        ApiClient.getApiService(this)
                .cancelMyServiceRequest(
                        currentOrder.serviceRequestId,
                        new CancelServiceRequestRequest(reason)
                )
                .enqueue(new Callback<ApiResponse<ServiceRequestResponse>>() {
                    @Override
                    public void onResponse(
                            Call<ApiResponse<ServiceRequestResponse>> call,
                            Response<ApiResponse<ServiceRequestResponse>> response
                    ) {
                        cancelling = false;

                        if (!response.isSuccessful()) {
                            AppToast.error(
                                    ServiceRequestDetailActivity.this,
                                    ErrorParser.parseError(response, "Order gagal dibatalkan.")
                            );

                            renderOrder(currentOrder);
                            return;
                        }

                        ApiResponse<ServiceRequestResponse> body = response.body();

                        if (body == null || !body.success || body.data == null) {
                            AppToast.error(
                                    ServiceRequestDetailActivity.this,
                                    ErrorParser.getBestMessage(body, "Order gagal dibatalkan.")
                            );

                            renderOrder(currentOrder);
                            return;
                        }

                        currentOrder = body.data;

                        if (dialog != null && dialog.isShowing()) {
                            dialog.dismiss();
                        }

                        AppToast.success(
                                ServiceRequestDetailActivity.this,
                                "Order berhasil dibatalkan."
                        );

                        renderOrder(currentOrder);
                        loadStatusHistory();
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<ServiceRequestResponse>> call, Throwable t) {
                        cancelling = false;

                        AppToast.error(
                                ServiceRequestDetailActivity.this,
                                "Tidak bisa terhubung ke server."
                        );

                        renderOrder(currentOrder);
                    }
                });
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private String getDetailMessage(ServiceRequestResponse order) {
        String status = OrderStatusHelper.normalize(order == null ? null : order.status);

        if ("WAITING".equals(status)) {
            return "Menunggu teknisi menerima request.";
        }

        if ("ACCEPTED".equals(status)) {
            return "Teknisi sudah menerima request.";
        }

        if ("ON_PROGRESS".equals(status)) {
            return "Pengerjaan sedang berlangsung.";
        }

        if ("COMPLETED".equals(status)) {
            String cost = formatMoney(order.finalCost);
            return isBlank(cost) ? "Order sudah selesai." : "Order selesai. Biaya akhir: " + cost;
        }

        if ("CANCELLED".equals(status)) {
            return "Order sudah dibatalkan.";
        }

        if ("REJECTED".equals(status)) {
            return "Order ditolak oleh teknisi.";
        }

        return "";
    }

    private String buildAddress(ServiceRequestResponse order) {
        String address = getSafeText(order == null ? null : order.address, "-");
        String detail = order == null ? null : order.addressDetail;

        if (isBlank(detail)) {
            return address;
        }

        return address + "\n\nDetail:\n" + detail.trim();
    }

    private String getCategoriesText(ServiceRequestResponse order) {
        if (order == null || order.selectedDeviceCategories == null || order.selectedDeviceCategories.isEmpty()) {
            return "-";
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

        return builder.length() == 0 ? "-" : builder.toString();
    }

    private String formatMoney(BigDecimal value) {
        if (value == null) {
            return "";
        }

        return "Rp" + value.toPlainString();
    }

    private void showError(String message) {
        setMessage(message);
        btnCancelOrder.setVisibility(View.GONE);
    }

    private void setMessage(String message) {
        txtDetailMessage.setText(message);
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

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}

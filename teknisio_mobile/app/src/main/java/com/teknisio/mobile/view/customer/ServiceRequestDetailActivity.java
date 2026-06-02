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
import com.teknisio.mobile.model.request.CreateReviewRequest;
import com.teknisio.mobile.model.response.ApiResponse;
import com.teknisio.mobile.model.response.DeviceCategoryResponse;
import com.teknisio.mobile.model.response.ReviewResponse;
import com.teknisio.mobile.model.response.ServiceRequestResponse;
import com.teknisio.mobile.model.response.StatusHistoryResponse;
import com.teknisio.mobile.network.ApiClient;
import com.teknisio.mobile.util.AppToast;
import com.teknisio.mobile.util.BackButtonHelper;
import com.teknisio.mobile.util.ErrorParser;
import com.teknisio.mobile.util.OrderStatusHelper;

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
            txtCancelReason.setText("Cancel reason:\n" + order.cancelReason.trim());
        } else if (!isBlank(order.rejectReason)) {
            txtCancelReason.setVisibility(View.VISIBLE);
            txtCancelReason.setText("Reject reason:\n" + order.rejectReason.trim());
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

        // Show review button only for COMPLETED orders
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
                        renderStatusHistory(response.body().data);
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<StatusHistoryResponse>>> call, Throwable t) {}
                });
    }

    private void renderStatusHistory(List<StatusHistoryResponse> historyList) {
        if (layoutStatusHistory == null) return;
        layoutStatusHistory.removeAllViews();
        if (historyList == null || historyList.isEmpty()) {
            txtStatusHistoryLabel.setVisibility(View.GONE);
            layoutStatusHistory.setVisibility(View.GONE);
            return;
        }
        txtStatusHistoryLabel.setVisibility(View.VISIBLE);
        layoutStatusHistory.setVisibility(View.VISIBLE);
        for (StatusHistoryResponse item : historyList) {
            layoutStatusHistory.addView(createHistoryRow(item));
        }
    }

    private View createHistoryRow(StatusHistoryResponse item) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, dp(6), 0, dp(6));

        // Dot/indicator
        View dot = new View(this);
        LinearLayout.LayoutParams dotParams = new LinearLayout.LayoutParams(dp(10), dp(10));
        dotParams.setMargins(0, dp(6), dp(12), 0);
        dot.setLayoutParams(dotParams);
        GradientDrawable dotDrawable = new GradientDrawable();
        dotDrawable.setShape(GradientDrawable.OVAL);
        dotDrawable.setColor(Color.parseColor(OrderStatusHelper.getStatusColor(item.newStatus)));
        dot.setBackground(dotDrawable);

        // Text container
        LinearLayout textCol = new LinearLayout(this);
        textCol.setOrientation(LinearLayout.VERTICAL);
        textCol.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView txtStatus = new TextView(this);
        txtStatus.setText(OrderStatusHelper.getDisplayStatus(item.newStatus));
        txtStatus.setTextColor(Color.parseColor("#1F2329"));
        txtStatus.setTextSize(14);
        txtStatus.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);

        TextView txtTime = new TextView(this);
        txtTime.setText(formatHistoryTime(item.changedAt));
        txtTime.setTextColor(Color.parseColor("#6B7680"));
        txtTime.setTextSize(12);

        if (item.note != null && !item.note.trim().isEmpty()) {
            TextView txtNote = new TextView(this);
            txtNote.setText(item.note.trim());
            txtNote.setTextColor(Color.parseColor("#5F6B73"));
            txtNote.setTextSize(13);
            LinearLayout.LayoutParams noteParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            noteParams.setMargins(0, dp(2), 0, 0);
            txtNote.setLayoutParams(noteParams);
            textCol.addView(txtStatus);
            textCol.addView(txtTime);
            textCol.addView(txtNote);
        } else {
            textCol.addView(txtStatus);
            textCol.addView(txtTime);
        }

        row.addView(dot);
        row.addView(textCol);
        return row;
    }

    private String formatHistoryTime(String isoTime) {
        if (isoTime == null || isoTime.trim().isEmpty()) return "-";
        try {
            java.time.OffsetDateTime odt = java.time.OffsetDateTime.parse(isoTime);
            java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter
                    .ofPattern("dd MMM yyyy, HH:mm", new java.util.Locale("id", "ID"));
            return odt.format(fmt);
        } catch (Exception e) {
            return isoTime;
        }
    }

    // -------------------------------------------------------------------------
    // Review Dialog
    // -------------------------------------------------------------------------

    private void showReviewDialog() {
        if (currentOrder == null) return;

        Dialog reviewDialog = new Dialog(this);
        reviewDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        // Build dialog content programmatically
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(dp(24), dp(24), dp(24), dp(24));
        container.setBackgroundResource(R.drawable.bg_dialog_card);

        TextView title = new TextView(this);
        title.setText("Tulis Ulasan");
        title.setTextColor(Color.parseColor("#1F2329"));
        title.setTextSize(18);
        title.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);

        TextView ratingLabel = new TextView(this);
        ratingLabel.setText("Rating (1-5)");
        ratingLabel.setTextColor(Color.parseColor("#6B7680"));
        ratingLabel.setTextSize(13);
        LinearLayout.LayoutParams ratingLabelParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        ratingLabelParams.setMargins(0, dp(16), 0, dp(4));
        ratingLabel.setLayoutParams(ratingLabelParams);

        EditText edtRating = new EditText(this);
        edtRating.setHint("Masukkan rating 1-5");
        edtRating.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        edtRating.setSingleLine(true);
        edtRating.setBackground(getResources().getDrawable(R.drawable.bg_dialog_input, getTheme()));

        TextView commentLabel = new TextView(this);
        commentLabel.setText("Komentar (opsional)");
        commentLabel.setTextColor(Color.parseColor("#6B7680"));
        commentLabel.setTextSize(13);
        LinearLayout.LayoutParams commentLabelParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        commentLabelParams.setMargins(0, dp(12), 0, dp(4));
        commentLabel.setLayoutParams(commentLabelParams);

        EditText edtComment = new EditText(this);
        edtComment.setHint("Bagikan pengalaman kamu...");
        edtComment.setSingleLine(false);
        edtComment.setMaxLines(4);
        edtComment.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        edtComment.setBackground(getResources().getDrawable(R.drawable.bg_dialog_input, getTheme()));

        TextView errorText = new TextView(this);
        errorText.setTextColor(Color.parseColor("#C62828"));
        errorText.setTextSize(12);
        errorText.setVisibility(View.GONE);
        LinearLayout.LayoutParams errorParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        errorParams.setMargins(0, dp(6), 0, 0);
        errorText.setLayoutParams(errorParams);

        // Buttons row
        LinearLayout btnRow = new LinearLayout(this);
        btnRow.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams btnRowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        btnRowParams.setMargins(0, dp(20), 0, 0);
        btnRow.setLayoutParams(btnRowParams);

        Button btnCancel = new Button(this);
        btnCancel.setText("Batal");
        btnCancel.setAllCaps(false);
        btnCancel.setBackground(getResources().getDrawable(R.drawable.bg_dialog_secondary_button, getTheme()));
        LinearLayout.LayoutParams cancelParams = new LinearLayout.LayoutParams(0, dp(46), 1f);
        cancelParams.setMargins(0, 0, dp(8), 0);
        btnCancel.setLayoutParams(cancelParams);
        btnCancel.setOnClickListener(v -> reviewDialog.dismiss());

        Button btnSubmit = new Button(this);
        btnSubmit.setText("Kirim");
        btnSubmit.setAllCaps(false);
        btnSubmit.setBackground(getResources().getDrawable(R.drawable.bg_order_primary, getTheme()));
        btnSubmit.setTextColor(Color.parseColor("#D4FFFF"));
        LinearLayout.LayoutParams submitParams = new LinearLayout.LayoutParams(0, dp(46), 1f);
        submitParams.setMargins(dp(8), 0, 0, 0);
        btnSubmit.setLayoutParams(submitParams);
        btnSubmit.setOnClickListener(v -> {
            String ratingStr = edtRating.getText() == null ? "" : edtRating.getText().toString().trim();
            if (ratingStr.isEmpty()) {
                errorText.setText("Rating wajib diisi.");
                errorText.setVisibility(View.VISIBLE);
                return;
            }
            int ratingVal;
            try {
                ratingVal = Integer.parseInt(ratingStr);
            } catch (Exception e) {
                errorText.setText("Rating harus berupa angka.");
                errorText.setVisibility(View.VISIBLE);
                return;
            }
            if (ratingVal < 1 || ratingVal > 5) {
                errorText.setText("Rating harus antara 1 dan 5.");
                errorText.setVisibility(View.VISIBLE);
                return;
            }
            String comment = edtComment.getText() == null ? null : edtComment.getText().toString().trim();
            errorText.setVisibility(View.GONE);
            btnSubmit.setEnabled(false);
            submitReview(ratingVal, (comment == null || comment.isEmpty()) ? null : comment, reviewDialog);
        });

        btnRow.addView(btnCancel);
        btnRow.addView(btnSubmit);

        container.addView(title);
        container.addView(ratingLabel);
        container.addView(edtRating);
        container.addView(commentLabel);
        container.addView(edtComment);
        container.addView(errorText);
        container.addView(btnRow);

        reviewDialog.setContentView(container);
        reviewDialog.setCanceledOnTouchOutside(true);
        reviewDialog.show();

        Window window = reviewDialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));
            window.setLayout(
                    getResources().getDisplayMetrics().widthPixels - dp(44),
                    WindowManager.LayoutParams.WRAP_CONTENT
            );
        }
    }

    private void submitReview(int rating, String comment, Dialog dialog) {
        ApiClient.getApiService(this)
                .createReview(serviceRequestId, new CreateReviewRequest(rating, comment))
                .enqueue(new Callback<ApiResponse<ReviewResponse>>() {
                    @Override
                    public void onResponse(
                            Call<ApiResponse<ReviewResponse>> call,
                            Response<ApiResponse<ReviewResponse>> response
                    ) {
                        if (!response.isSuccessful()) {
                            AppToast.error(ServiceRequestDetailActivity.this,
                                    ErrorParser.parseError(response, "Gagal mengirim ulasan."));
                            return;
                        }
                        ApiResponse<ReviewResponse> body = response.body();
                        if (body == null || !body.success) {
                            AppToast.error(ServiceRequestDetailActivity.this,
                                    ErrorParser.getBestMessage(body, "Gagal mengirim ulasan."));
                            return;
                        }
                        if (dialog != null && dialog.isShowing()) dialog.dismiss();
                        hasReview = true;
                        btnWriteReview.setVisibility(View.GONE);
                        AppToast.success(ServiceRequestDetailActivity.this, "Ulasan berhasil dikirim!");
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<ReviewResponse>> call, Throwable t) {
                        AppToast.error(ServiceRequestDetailActivity.this, "Tidak bisa terhubung ke server.");
                    }
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

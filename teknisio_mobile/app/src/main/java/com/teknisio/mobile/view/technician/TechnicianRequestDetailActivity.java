package com.teknisio.mobile.view.technician;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import com.teknisio.mobile.R;
import com.teknisio.mobile.base.BaseActivity;
import com.teknisio.mobile.model.request.CompleteServiceRequestRequest;
import com.teknisio.mobile.model.request.RejectServiceRequestRequest;
import com.teknisio.mobile.model.response.ApiResponse;
import com.teknisio.mobile.model.response.ServiceRequestResponse;
import com.teknisio.mobile.network.ApiClient;
import com.teknisio.mobile.util.AppToast;
import com.teknisio.mobile.util.BackButtonHelper;
import com.teknisio.mobile.util.ErrorParser;
import com.teknisio.mobile.model.response.StatusHistoryResponse;
import com.teknisio.mobile.util.OrderStatusHelper;
import com.teknisio.mobile.util.StatusHistoryRenderer;
import com.teknisio.mobile.util.TextHelper;
import com.teknisio.mobile.util.ViewHelper;
import com.teknisio.mobile.view.customer.ChatActivity;
import com.teknisio.mobile.view.technician.helper.TechnicianRequestActionDialogHelper;
import com.teknisio.mobile.view.tracking.TrackingMapActivity;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TechnicianRequestDetailActivity extends BaseActivity {

    public static final String EXTRA_SERVICE_REQUEST_ID = "extra_service_request_id";

    private FrameLayout btnBack;
    private TextView txtTechOrderCode;
    private TextView txtTechOrderStatus;
    private TextView txtTechOrderTime;
    private TextView txtTechCustomer;
    private TextView txtTechCategories;
    private TextView txtTechIssue;
    private TextView txtTechAddress;
    private TextView txtTechCost;
    private TextView txtTechNote;
    private TextView txtTechDetailMessage;

    private LinearLayout layoutTechnicianActionPanel;
    private LinearLayout layoutStatusHistory;
    private TextView txtStatusHistoryLabel;
    private AppCompatButton btnAcceptRequest;
    private AppCompatButton btnRejectRequest;
    private AppCompatButton btnStartRequest;
    private AppCompatButton btnCompleteRequest;
    private AppCompatButton btnNavigateToCustomer;
    private AppCompatButton btnTechnicianChat;

    private String serviceRequestId;
    private ServiceRequestResponse currentServiceRequest;
    private boolean loading = false;
    private boolean actionLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_technician_request_detail);

        serviceRequestId = getIntent().getStringExtra(EXTRA_SERVICE_REQUEST_ID);

        bindViews();
        setupActions();

        if (isBlank(serviceRequestId)) {
            AppToast.error(this, "Data request tidak valid.");
            finish();
            return;
        }

        loadDetail();
    }

    private void bindViews() {
        btnBack = findViewById(R.id.btnBack);
        txtTechOrderCode = findViewById(R.id.txtTechOrderCode);
        txtTechOrderStatus = findViewById(R.id.txtTechOrderStatus);
        txtTechOrderTime = findViewById(R.id.txtTechOrderTime);
        txtTechCustomer = findViewById(R.id.txtTechCustomer);
        txtTechCategories = findViewById(R.id.txtTechCategories);
        txtTechIssue = findViewById(R.id.txtTechIssue);
        txtTechAddress = findViewById(R.id.txtTechAddress);
        txtTechCost = findViewById(R.id.txtTechCost);
        txtTechNote = findViewById(R.id.txtTechNote);
        txtTechDetailMessage = findViewById(R.id.txtTechDetailMessage);

        layoutTechnicianActionPanel = findViewById(R.id.layoutTechnicianActionPanel);
        layoutStatusHistory = findViewById(R.id.layoutStatusHistory);
        txtStatusHistoryLabel = findViewById(R.id.txtStatusHistoryLabel);
        btnAcceptRequest = findViewById(R.id.btnAcceptRequest);
        btnRejectRequest = findViewById(R.id.btnRejectRequest);
        btnStartRequest = findViewById(R.id.btnStartRequest);
        btnCompleteRequest = findViewById(R.id.btnCompleteRequest);
        btnNavigateToCustomer = findViewById(R.id.btnNavigateToCustomer);
        btnTechnicianChat = findViewById(R.id.btnTechnicianChat);
    }

    private void setupActions() {
        BackButtonHelper.setup(btnBack, this::finish);

        btnAcceptRequest.setOnClickListener(v -> confirmAccept());
        btnRejectRequest.setOnClickListener(v -> showRejectDialog());
        btnStartRequest.setOnClickListener(v -> confirmStart());
        btnCompleteRequest.setOnClickListener(v -> showCompleteDialog());
        if (btnNavigateToCustomer != null) {
            btnNavigateToCustomer.setOnClickListener(v -> openNavigationMap());
        }
        if (btnTechnicianChat != null) {
            btnTechnicianChat.setOnClickListener(v -> openChat());
        }
    }

    private void loadDetail() {
        if (loading) {
            return;
        }

        loading = true;
        showLoading("Memuat detail request...");

        ApiClient.getApiService(this)
                .getTechnicianServiceRequestDetail(serviceRequestId)
                .enqueue(new Callback<ApiResponse<ServiceRequestResponse>>() {
                    @Override
                    public void onResponse(
                            Call<ApiResponse<ServiceRequestResponse>> call,
                            Response<ApiResponse<ServiceRequestResponse>> response
                    ) {
                        loading = false;

                        if (!response.isSuccessful()) {
                            showError(ErrorParser.parseError(response, "Detail request gagal dimuat."));
                            return;
                        }

                        ApiResponse<ServiceRequestResponse> body = response.body();

                        if (body == null || !body.success || body.data == null) {
                            showError(ErrorParser.getBestMessage(body, "Detail request gagal dimuat."));
                            return;
                        }

                        renderDetail(body.data);
                        currentServiceRequest = body.data;
                        loadStatusHistory();
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<ServiceRequestResponse>> call, Throwable t) {
                        loading = false;
                        showError("Tidak bisa terhubung ke server.");
                    }
                });
    }

    private void renderDetail(ServiceRequestResponse request) {
        txtTechOrderCode.setText(getSafeText(request.serviceRequestCode, "Request"));
        txtTechOrderStatus.setText(OrderStatusHelper.getDisplayStatus(request.status));
        txtTechOrderStatus.setBackground(makeRounded(OrderStatusHelper.getStatusColor(request.status), 16));
        txtTechOrderTime.setText("Waktu request: " + TextHelper.formatDateTime(request.requestTime));

        txtTechCustomer.setText(buildCustomerText(request));
        txtTechCategories.setText(getCategoriesText(request));
        txtTechIssue.setText(getSafeText(request.issueDescription, "-"));
        txtTechAddress.setText(buildAddressText(request));

        renderCostAndNote(request);
        renderActionPanel(request);
    }

    private void renderCostAndNote(ServiceRequestResponse request) {
        txtTechCost.setVisibility(View.GONE);
        txtTechNote.setVisibility(View.GONE);

        if (request == null) {
            return;
        }

        if (request.finalCost != null) {
            txtTechCost.setVisibility(View.VISIBLE);
            txtTechCost.setText("Biaya akhir: " + formatCurrency(request.finalCost));
        }

        String normalized = OrderStatusHelper.normalize(request.status);

        if ("COMPLETED".equals(normalized) && !isBlank(request.technicianNote)) {
            txtTechNote.setVisibility(View.VISIBLE);
            txtTechNote.setText("Catatan teknisi:\n" + request.technicianNote.trim());
            return;
        }

        if ("REJECTED".equals(normalized) && !isBlank(request.rejectReason)) {
            txtTechNote.setVisibility(View.VISIBLE);
            txtTechNote.setText("Alasan penolakan:\n" + request.rejectReason.trim());
            return;
        }

        if ("CANCELLED".equals(normalized) && !isBlank(request.cancelReason)) {
            txtTechNote.setVisibility(View.VISIBLE);
            txtTechNote.setText("Alasan pembatalan:\n" + request.cancelReason.trim());
        }
    }

    private void renderActionPanel(ServiceRequestResponse request) {
        hideAllActionButtons();

        if (request == null) {
            layoutTechnicianActionPanel.setVisibility(View.VISIBLE);
            txtTechDetailMessage.setText("Data request tidak valid.");
            return;
        }

        String status = OrderStatusHelper.normalize(request.status);
        layoutTechnicianActionPanel.setVisibility(View.VISIBLE);

        switch (status) {
            case "WAITING":
                txtTechDetailMessage.setText("Request baru masuk. Terima jika kamu siap mengerjakan, atau tolak jika tidak sesuai.");
                btnAcceptRequest.setVisibility(View.VISIBLE);
                btnRejectRequest.setVisibility(View.VISIBLE);
                break;

            case "ACCEPTED":
                txtTechDetailMessage.setText("Request sudah diterima. Mulai pengerjaan saat kamu sudah berada di lokasi atau siap bekerja.");
                btnStartRequest.setVisibility(View.VISIBLE);
                btnNavigateToCustomer.setVisibility(View.VISIBLE);
                if (btnTechnicianChat != null) btnTechnicianChat.setVisibility(View.VISIBLE);
                break;

            case "ON_PROGRESS":
                txtTechDetailMessage.setText("Request sedang dikerjakan. Bagikan lokasi tetap aktif agar pelanggan bisa memantau teknisi.");
                btnCompleteRequest.setVisibility(View.VISIBLE);
                btnNavigateToCustomer.setVisibility(View.VISIBLE);
                if (btnTechnicianChat != null) btnTechnicianChat.setVisibility(View.VISIBLE);
                break;

            case "COMPLETED":
                txtTechDetailMessage.setText("Pekerjaan sudah selesai.");
                break;

            case "REJECTED":
                txtTechDetailMessage.setText("Request ini sudah ditolak.");
                break;

            case "CANCELLED":
                txtTechDetailMessage.setText("Request ini sudah dibatalkan pelanggan.");
                break;

            default:
                txtTechDetailMessage.setText("Tidak ada aksi tersedia untuk status ini.");
                break;
        }

        setActionLoading(false);
    }

    private void hideAllActionButtons() {
        btnAcceptRequest.setVisibility(View.GONE);
        btnRejectRequest.setVisibility(View.GONE);
        btnStartRequest.setVisibility(View.GONE);
        btnCompleteRequest.setVisibility(View.GONE);
        btnNavigateToCustomer.setVisibility(View.GONE);
        if (btnTechnicianChat != null) btnTechnicianChat.setVisibility(View.GONE);
    }

    private void openChat() {
        if (currentServiceRequest == null) return;

        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(ChatActivity.EXTRA_SERVICE_REQUEST_ID, currentServiceRequest.serviceRequestId);
        intent.putExtra(ChatActivity.EXTRA_CHAT_PARTNER_NAME,
                currentServiceRequest.customerName != null
                        ? currentServiceRequest.customerName
                        : "Pelanggan");
        startActivity(intent);
    }

    private void openNavigationMap() {
        Intent intent = new Intent(this, TrackingMapActivity.class);
        intent.putExtra(TrackingMapActivity.EXTRA_MODE, TrackingMapActivity.MODE_TECHNICIAN);
        intent.putExtra(TrackingMapActivity.EXTRA_SERVICE_REQUEST_ID, serviceRequestId);

        if (currentServiceRequest != null) {
            // Use actual coordinates stored in the service request
            double lat = 0.0;
            double lng = 0.0;
            // ServiceRequestResponse uses BigDecimal for lat/lng
            if (currentServiceRequest.latitude != null) {
                lat = currentServiceRequest.latitude.doubleValue();
            }
            if (currentServiceRequest.longitude != null) {
                lng = currentServiceRequest.longitude.doubleValue();
            }
            intent.putExtra(TrackingMapActivity.EXTRA_CUSTOMER_LAT, lat);
            intent.putExtra(TrackingMapActivity.EXTRA_CUSTOMER_LNG, lng);
            intent.putExtra(TrackingMapActivity.EXTRA_TECHNICIAN_NAME,
                    currentServiceRequest.customerName != null
                            ? currentServiceRequest.customerName
                            : "Pelanggan");
        }

        startActivity(intent);
    }

    private void confirmAccept() {
        TechnicianRequestActionDialogHelper.confirmAccept(this, this::acceptRequest);
    }

    private void confirmStart() {
        TechnicianRequestActionDialogHelper.confirmStart(this, this::startRequest);
    }

    private void showRejectDialog() {
        TechnicianRequestActionDialogHelper.showRejectDialog(this, this::rejectRequest);
    }

    private void showCompleteDialog() {
        TechnicianRequestActionDialogHelper.showCompleteDialog(this, this::completeRequest);
    }

    private void acceptRequest() {
        performAction(
                "Menerima request...",
                ApiClient.getApiService(this).acceptTechnicianServiceRequest(serviceRequestId),
                "Request berhasil diterima."
        );
    }

    private void rejectRequest(String reason) {
        performAction(
                "Menolak request...",
                ApiClient.getApiService(this).rejectTechnicianServiceRequest(
                        serviceRequestId,
                        new RejectServiceRequestRequest(isBlank(reason) ? null : reason.trim())
                ),
                "Request berhasil ditolak."
        );
    }

    private void startRequest() {
        performAction(
                "Memulai pengerjaan...",
                ApiClient.getApiService(this).startTechnicianServiceRequest(serviceRequestId),
                "Pengerjaan berhasil dimulai."
        );
    }

    private void completeRequest(BigDecimal finalCost, String note) {
        performAction(
                "Menyelesaikan pekerjaan...",
                ApiClient.getApiService(this).completeTechnicianServiceRequest(
                        serviceRequestId,
                        new CompleteServiceRequestRequest(finalCost, isBlank(note) ? null : note.trim())
                ),
                "Pekerjaan berhasil diselesaikan."
        );
    }

    private void performAction(
            String loadingMessage,
            Call<ApiResponse<ServiceRequestResponse>> call,
            String successMessage
    ) {
        if (actionLoading) {
            return;
        }

        actionLoading = true;
        setActionLoading(true);
        txtTechDetailMessage.setText(loadingMessage);

        call.enqueue(new Callback<ApiResponse<ServiceRequestResponse>>() {
            @Override
            public void onResponse(
                    Call<ApiResponse<ServiceRequestResponse>> call,
                    Response<ApiResponse<ServiceRequestResponse>> response
            ) {
                actionLoading = false;

                if (!response.isSuccessful()) {
                    setActionLoading(false);
                    txtTechDetailMessage.setText(ErrorParser.parseError(response, "Aksi request gagal diproses."));
                    AppToast.error(TechnicianRequestDetailActivity.this, "Aksi gagal diproses.");
                    return;
                }

                ApiResponse<ServiceRequestResponse> body = response.body();

                if (body == null || !body.success || body.data == null) {
                    setActionLoading(false);
                    txtTechDetailMessage.setText(ErrorParser.getBestMessage(body, "Aksi request gagal diproses."));
                    AppToast.error(TechnicianRequestDetailActivity.this, "Aksi gagal diproses.");
                    return;
                }

                setResult(RESULT_OK);
                AppToast.success(TechnicianRequestDetailActivity.this, successMessage);
                renderDetail(body.data);
                loadStatusHistory();
            }

            @Override
            public void onFailure(Call<ApiResponse<ServiceRequestResponse>> call, Throwable t) {
                actionLoading = false;
                setActionLoading(false);
                txtTechDetailMessage.setText("Tidak bisa terhubung ke server.");
                AppToast.error(TechnicianRequestDetailActivity.this, "Tidak bisa terhubung ke server.");
            }
        });
    }

    // -------------------------------------------------------------------------
    // Status History
    // -------------------------------------------------------------------------

    private void loadStatusHistory() {
        ApiClient.getApiService(this)
                .getTechnicianServiceRequestStatusHistory(serviceRequestId)
                .enqueue(new Callback<ApiResponse<List<StatusHistoryResponse>>>() {
                    @Override
                    public void onResponse(
                            Call<ApiResponse<List<StatusHistoryResponse>>> call,
                            Response<ApiResponse<List<StatusHistoryResponse>>> response
                    ) {
                        if (!response.isSuccessful() || response.body() == null
                                || !response.body().success || response.body().data == null) return;
                        StatusHistoryRenderer.render(TechnicianRequestDetailActivity.this, layoutStatusHistory, txtStatusHistoryLabel, response.body().data);
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<StatusHistoryResponse>>> call, Throwable t) {}
                });
    }

    private void setActionLoading(boolean value) {
        btnAcceptRequest.setEnabled(!value);
        btnRejectRequest.setEnabled(!value);
        btnStartRequest.setEnabled(!value);
        btnCompleteRequest.setEnabled(!value);
    }

    private void showLoading(String message) {
        layoutTechnicianActionPanel.setVisibility(View.VISIBLE);
        hideAllActionButtons();
        txtTechDetailMessage.setText(message);
    }

    private void showError(String message) {
        layoutTechnicianActionPanel.setVisibility(View.VISIBLE);
        hideAllActionButtons();
        txtTechDetailMessage.setText(getSafeText(message, "Terjadi kesalahan."));
    }

    private String buildCustomerText(ServiceRequestResponse request) {
        String name = getSafeText(request == null ? null : request.customerName, "-");
        String phone = getSafeText(request == null ? null : request.customerPhoneNumber, "-");

        return "Nama: " + name + "\nNomor telepon: " + phone;
    }

    private String buildAddressText(ServiceRequestResponse request) {
        String address = getSafeText(request == null ? null : request.address, "-");
        String detail = request == null ? null : request.addressDetail;

        if (isBlank(detail)) {
            return address;
        }

        return address + "\n" + detail.trim();
    }

    private String getCategoriesText(ServiceRequestResponse request) {
        return TextHelper.deviceCategoriesText(
                request == null ? null : request.selectedDeviceCategories,
                "Kategori perangkat tidak tersedia",
                ", "
        );
    }

    private String formatCurrency(BigDecimal value) {
        if (value == null) {
            return "-";
        }

        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        formatter.setMaximumFractionDigits(0);

        return formatter.format(value);
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

    private int dp(int value) {
        return ViewHelper.dp(this, value);
    }
}

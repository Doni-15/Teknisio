package com.teknisio.mobile.view.customer;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.teknisio.mobile.R;
import com.teknisio.mobile.local.TokenManager;
import com.teknisio.mobile.model.request.CreateServiceRequestRequest;
import com.teknisio.mobile.model.response.ApiResponse;
import com.teknisio.mobile.model.response.CustomerTechnicianResponse;
import com.teknisio.mobile.model.response.DeviceCategoryResponse;
import com.teknisio.mobile.model.response.ServiceRequestResponse;
import com.teknisio.mobile.network.ApiClient;
import com.teknisio.mobile.util.BackButtonHelper;
import com.teknisio.mobile.util.ErrorParser;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderTechnicianActivity extends AppCompatActivity {

    public static final String EXTRA_CATEGORY_ID = "extra_category_id";
    public static final String EXTRA_CATEGORY_NAME = "extra_category_name";
    public static final String EXTRA_TECHNICIAN_ID = "extra_technician_id";
    public static final String EXTRA_TECHNICIAN_NAME = "extra_technician_name";

    private FrameLayout btnBack;
    private ImageView imgCategoryIcon;
    private TextView txtCategoryName;
    private TextView txtTechnicianName;
    private TextView txtTechnicianMeta;
    private LinearLayout layoutSelectedCategories;
    private TextView txtSelectedSummary;
    private EditText edtIssueDescription;
    private EditText edtAddress;
    private EditText edtAddressDetail;
    private Button btnConfirmOrder;

    private TokenManager tokenManager;

    private String categoryId;
    private String categoryName;
    private String technicianId;
    private String technicianName;

    private CustomerTechnicianResponse selectedTechnician;
    private final List<String> selectedCategoryIds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_technician);

        tokenManager = new TokenManager(this);

        categoryId = getIntent().getStringExtra(EXTRA_CATEGORY_ID);
        categoryName = getIntent().getStringExtra(EXTRA_CATEGORY_NAME);
        technicianId = getIntent().getStringExtra(EXTRA_TECHNICIAN_ID);
        technicianName = getIntent().getStringExtra(EXTRA_TECHNICIAN_NAME);

        bindViews();
        setupInitialData();
        setupActions();
        loadSelectedTechnician();
    }

    private void bindViews() {
        btnBack = findViewById(R.id.btnBack);
        imgCategoryIcon = findViewById(R.id.imgCategoryIcon);
        txtCategoryName = findViewById(R.id.txtCategoryName);
        txtTechnicianName = findViewById(R.id.txtTechnicianName);
        txtTechnicianMeta = findViewById(R.id.txtTechnicianMeta);
        layoutSelectedCategories = findViewById(R.id.layoutSelectedCategories);
        txtSelectedSummary = findViewById(R.id.txtSelectedSummary);
        edtIssueDescription = findViewById(R.id.edtIssueDescription);
        edtAddress = findViewById(R.id.edtAddress);
        edtAddressDetail = findViewById(R.id.edtAddressDetail);
        btnConfirmOrder = findViewById(R.id.btnConfirmOrder);
    }

    private void setupInitialData() {
        if (isBlank(categoryId) || isBlank(technicianId)) {
            Toast.makeText(this, "Data order tidak valid.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        txtCategoryName.setText(getCleanCategoryName(categoryName));
        imgCategoryIcon.setImageResource(getCategoryIconResId(categoryName));

        txtTechnicianName.setText(isBlank(technicianName) ? "Teknisi" : technicianName);

        String address = tokenManager.getAddress();
        if (!isBlank(address)) {
            edtAddress.setText(address.trim());
        }

        setLoading(false);
    }

    private void setupActions() {
        BackButtonHelper.setup(btnBack, this::finish);
        btnConfirmOrder.setOnClickListener(v -> confirmOrder());
    }

    private void loadSelectedTechnician() {
        setLoading(true);
        txtSelectedSummary.setText("Memuat kategori teknisi...");

        ApiClient.getApiService(this)
                .searchTechnicians(categoryId, null, "rating")
                .enqueue(new Callback<ApiResponse<List<CustomerTechnicianResponse>>>() {
                    @Override
                    public void onResponse(
                            Call<ApiResponse<List<CustomerTechnicianResponse>>> call,
                            Response<ApiResponse<List<CustomerTechnicianResponse>>> response
                    ) {
                        setLoading(false);

                        if (!response.isSuccessful()) {
                            Toast.makeText(
                                    OrderTechnicianActivity.this,
                                    ErrorParser.parseError(response, "Teknisi gagal dimuat."),
                                    Toast.LENGTH_LONG
                            ).show();
                            finish();
                            return;
                        }

                        ApiResponse<List<CustomerTechnicianResponse>> body = response.body();

                        if (body == null || !body.success || body.data == null) {
                            Toast.makeText(
                                    OrderTechnicianActivity.this,
                                    ErrorParser.getBestMessage(body, "Teknisi gagal dimuat."),
                                    Toast.LENGTH_LONG
                            ).show();
                            finish();
                            return;
                        }

                        for (CustomerTechnicianResponse technician : body.data) {
                            if (technician != null
                                    && !isBlank(technician.technicianProfileId)
                                    && technician.technicianProfileId.equals(technicianId)) {
                                selectedTechnician = technician;
                                break;
                            }
                        }

                        if (selectedTechnician == null) {
                            Toast.makeText(
                                    OrderTechnicianActivity.this,
                                    "Teknisi tidak tersedia untuk kategori ini.",
                                    Toast.LENGTH_LONG
                            ).show();
                            finish();
                            return;
                        }

                        renderSelectedTechnician();
                        resetSelectedCategories();
                        renderSelectedCategories();
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<CustomerTechnicianResponse>>> call, Throwable t) {
                        setLoading(false);
                        Toast.makeText(
                                OrderTechnicianActivity.this,
                                "Tidak bisa terhubung ke server.",
                                Toast.LENGTH_LONG
                        ).show();
                        finish();
                    }
                });
    }

    private void renderSelectedTechnician() {
        txtTechnicianName.setText(isBlank(selectedTechnician.name) ? "Teknisi" : selectedTechnician.name);

        txtTechnicianMeta.setText(
                "★ " + formatRating(selectedTechnician.averageRating)
                        + "  •  " + formatJobs(selectedTechnician.totalJobs)
                        + "\n" + getTechnicianCategories(selectedTechnician)
        );
    }

    private void resetSelectedCategories() {
        selectedCategoryIds.clear();

        if (isCategorySupportedByTechnician(categoryId)) {
            selectedCategoryIds.add(categoryId);
            return;
        }

        List<DeviceCategoryResponse> categories = getSelectableCategories();

        if (!categories.isEmpty()
                && categories.get(0) != null
                && !isBlank(categories.get(0).deviceCategoryId)) {
            selectedCategoryIds.add(categories.get(0).deviceCategoryId);
        }
    }

    private void renderSelectedCategories() {
        layoutSelectedCategories.removeAllViews();

        TextView hint = new TextView(this);
        hint.setText("Pilih kategori perangkat yang ingin diperbaiki oleh teknisi ini.");
        hint.setTextColor(Color.parseColor("#6B7680"));
        hint.setTextSize(13);
        layoutSelectedCategories.addView(hint);

        txtSelectedSummary.setText(getSelectedCategorySummary());

        List<DeviceCategoryResponse> categories = getSelectableCategories();

        if (categories.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("Belum ada kategori dari teknisi ini.");
            empty.setTextColor(Color.parseColor("#8A949B"));
            empty.setTextSize(14);
            layoutSelectedCategories.addView(empty);
            setLoading(false);
            return;
        }

        LinearLayout row = null;

        for (int i = 0; i < categories.size(); i++) {
            if (i % 2 == 0) {
                row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);

                LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                rowParams.setMargins(0, dp(12), 0, 0);
                row.setLayoutParams(rowParams);

                layoutSelectedCategories.addView(row);
            }

            if (row != null) {
                row.addView(createCategoryChip(categories.get(i)));
            }
        }

        setLoading(false);
    }

    private TextView createCategoryChip(DeviceCategoryResponse category) {
        boolean selected = category != null
                && !isBlank(category.deviceCategoryId)
                && selectedCategoryIds.contains(category.deviceCategoryId);

        TextView chip = new TextView(this);
        chip.setText((selected ? "✓ " : "+ ") + getCleanCategoryName(category == null ? null : category.name));
        chip.setGravity(Gravity.CENTER);
        chip.setPadding(dp(12), dp(8), dp(12), dp(8));
        chip.setTextSize(13);
        chip.setTypeface(Typeface.DEFAULT_BOLD);
        chip.setTextColor(selected ? Color.WHITE : Color.parseColor("#2F4A8A"));
        chip.setBackgroundResource(selected
                ? R.drawable.bg_category_chip_selected
                : R.drawable.bg_category_chip_unselected
        );

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(40), 1f);
        params.setMargins(0, 0, dp(8), 0);
        chip.setLayoutParams(params);

        chip.setOnClickListener(v -> {
            toggleCategorySelection(category);
            renderSelectedCategories();
        });

        return chip;
    }

    private void toggleCategorySelection(DeviceCategoryResponse category) {
        if (category == null || isBlank(category.deviceCategoryId)) {
            return;
        }

        if (selectedCategoryIds.contains(category.deviceCategoryId)) {
            if (selectedCategoryIds.size() == 1) {
                Toast.makeText(this, "Minimal 1 kategori harus dipilih.", Toast.LENGTH_SHORT).show();
                return;
            }

            selectedCategoryIds.remove(category.deviceCategoryId);
            return;
        }

        if (selectedCategoryIds.size() >= 10) {
            Toast.makeText(this, "Maksimal 10 kategori dalam satu order.", Toast.LENGTH_SHORT).show();
            return;
        }

        selectedCategoryIds.add(category.deviceCategoryId);
    }

    private void confirmOrder() {
        if (selectedTechnician == null || isBlank(selectedTechnician.technicianProfileId)) {
            Toast.makeText(this, "Teknisi tidak valid.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedCategoryIds.isEmpty()) {
            Toast.makeText(this, "Pilih minimal 1 kategori perangkat.", Toast.LENGTH_SHORT).show();
            return;
        }

        String issueDescription = edtIssueDescription.getText().toString().trim();
        String address = edtAddress.getText().toString().trim();
        String addressDetail = edtAddressDetail.getText().toString().trim();

        if (issueDescription.isEmpty()) {
            Toast.makeText(this, "Deskripsi kerusakan wajib diisi.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (address.isEmpty()) {
            Toast.makeText(this, "Alamat wajib diisi.", Toast.LENGTH_SHORT).show();
            return;
        }

        CreateServiceRequestRequest request = new CreateServiceRequestRequest(
                selectedTechnician.technicianProfileId,
                new ArrayList<>(selectedCategoryIds),
                issueDescription,
                address,
                addressDetail
        );

        setLoading(true);

        ApiClient.getApiService(this)
                .createServiceRequest(request)
                .enqueue(new Callback<ApiResponse<ServiceRequestResponse>>() {
                    @Override
                    public void onResponse(
                            Call<ApiResponse<ServiceRequestResponse>> call,
                            Response<ApiResponse<ServiceRequestResponse>> response
                    ) {
                        setLoading(false);

                        if (!response.isSuccessful()) {
                            Toast.makeText(
                                    OrderTechnicianActivity.this,
                                    ErrorParser.parseError(response, "Order gagal dibuat."),
                                    Toast.LENGTH_LONG
                            ).show();
                            return;
                        }

                        ApiResponse<ServiceRequestResponse> body = response.body();

                        if (body == null || !body.success) {
                            Toast.makeText(
                                    OrderTechnicianActivity.this,
                                    ErrorParser.getBestMessage(body, "Order gagal dibuat."),
                                    Toast.LENGTH_LONG
                            ).show();
                            return;
                        }

                        String code = body.data == null ? null : body.data.serviceRequestCode;
                        Toast.makeText(
                                OrderTechnicianActivity.this,
                                isBlank(code) ? "Order berhasil dibuat." : "Order berhasil dibuat: " + code,
                                Toast.LENGTH_LONG
                        ).show();

                        finish();
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<ServiceRequestResponse>> call, Throwable t) {
                        setLoading(false);
                        Toast.makeText(
                                OrderTechnicianActivity.this,
                                "Tidak bisa terhubung ke server.",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

    private List<DeviceCategoryResponse> getSelectableCategories() {
        if (selectedTechnician != null
                && selectedTechnician.supportedDeviceCategories != null
                && !selectedTechnician.supportedDeviceCategories.isEmpty()) {
            return selectedTechnician.supportedDeviceCategories;
        }

        List<DeviceCategoryResponse> fallback = new ArrayList<>();

        if (!isBlank(categoryId)) {
            DeviceCategoryResponse category = new DeviceCategoryResponse();
            category.deviceCategoryId = categoryId;
            category.name = categoryName;
            fallback.add(category);
        }

        return fallback;
    }

    private boolean isCategorySupportedByTechnician(String id) {
        if (isBlank(id)) {
            return false;
        }

        for (DeviceCategoryResponse category : getSelectableCategories()) {
            if (category != null
                    && !isBlank(category.deviceCategoryId)
                    && category.deviceCategoryId.equals(id)) {
                return true;
            }
        }

        return false;
    }

    private String getSelectedCategorySummary() {
        List<String> selectedNames = new ArrayList<>();

        for (DeviceCategoryResponse category : getSelectableCategories()) {
            if (category != null
                    && !isBlank(category.deviceCategoryId)
                    && selectedCategoryIds.contains(category.deviceCategoryId)) {
                selectedNames.add(getCleanCategoryName(category.name));
            }
        }

        if (selectedNames.isEmpty()) {
            return "Dipilih: belum ada";
        }

        return "Dipilih: " + TextUtils.join(", ", selectedNames);
    }

    private String getTechnicianCategories(CustomerTechnicianResponse technician) {
        if (technician == null
                || technician.supportedDeviceCategories == null
                || technician.supportedDeviceCategories.isEmpty()) {
            return getCleanCategoryName(categoryName);
        }

        List<String> names = new ArrayList<>();

        for (DeviceCategoryResponse category : technician.supportedDeviceCategories) {
            if (category != null && !isBlank(category.name)) {
                names.add(getCleanCategoryName(category.name));
            }
        }

        if (names.isEmpty()) {
            return getCleanCategoryName(categoryName);
        }

        return TextUtils.join(", ", names);
    }

    private void setLoading(boolean loading) {
        btnConfirmOrder.setEnabled(!loading && selectedTechnician != null && !selectedCategoryIds.isEmpty());
        btnConfirmOrder.setText(loading ? "Creating Order..." : "Confirm Order");
    }

    private String formatRating(BigDecimal rating) {
        if (rating == null) {
            return "0.0";
        }

        return rating.setScale(1, java.math.RoundingMode.HALF_UP).toPlainString();
    }

    private String formatJobs(Integer totalJobs) {
        return (totalJobs == null ? 0 : totalJobs) + " jobs";
    }

    private String getCleanCategoryName(String name) {
        if (isBlank(name)) {
            return "Device";
        }

        String lower = name.toLowerCase();

        if (lower.contains("air conditioner")) return "AC";
        if (lower.contains("refrigerator")) return "Fridge";
        if (lower.contains("television")) return "TV";

        return name.trim();
    }

    private int getCategoryIconResId(String name) {
        if (name == null) return R.drawable.ac;

        String lower = name.toLowerCase();

        if (lower.contains("washing")) return R.drawable.washing_machine;
        if (lower.contains("rice")) return R.drawable.rice_cooker;
        if (lower.contains("refrigerator") || lower.contains("fridge") || lower.contains("kulkas")) return R.drawable.refrigerator;
        if (lower.contains("oven")) return R.drawable.oven;
        if (lower.contains("television") || lower.equals("tv") || lower.contains("televisi")) return R.drawable.television;
        if (lower.contains("fan") || lower.contains("kipas")) return R.drawable.fan;
        if (lower.contains("mixer")) return R.drawable.mixer;
        if (lower.equals("ac") || lower.contains("air conditioner") || lower.contains("pendingin")) return R.drawable.ac;

        return R.drawable.ac;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}

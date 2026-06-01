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
import com.teknisio.mobile.model.response.CustomerTechnicianResponse;
import com.teknisio.mobile.model.response.DeviceCategoryResponse;
import com.teknisio.mobile.network.ApiClient;
import com.teknisio.mobile.util.BackButtonHelper;
import com.teknisio.mobile.util.ErrorParser;

import java.math.BigDecimal;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TechnicianDetailActivity extends BaseActivity {

    public static final String EXTRA_TECHNICIAN_ID = "extra_technician_id";
    public static final String EXTRA_TECHNICIAN_NAME = "extra_technician_name";
    public static final String EXTRA_CATEGORY_ID = "extra_category_id";
    public static final String EXTRA_CATEGORY_NAME = "extra_category_name";

    private FrameLayout btnBack;
    private TextView txtTechnicianInitial;
    private TextView txtTechnicianName;
    private TextView txtTechnicianStatus;
    private TextView txtRating;
    private TextView txtJobs;
    private TextView txtReviews;
    private TextView txtDescription;
    private LinearLayout layoutSupportedCategories;
    private TextView txtDetailMessage;
    private Button btnOrderTechnician;

    private String technicianId;
    private String technicianName;
    private String categoryId;
    private String categoryName;

    private CustomerTechnicianResponse technicianDetail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_technician_detail);

        technicianId = getIntent().getStringExtra(EXTRA_TECHNICIAN_ID);
        technicianName = getIntent().getStringExtra(EXTRA_TECHNICIAN_NAME);
        categoryId = getIntent().getStringExtra(EXTRA_CATEGORY_ID);
        categoryName = getIntent().getStringExtra(EXTRA_CATEGORY_NAME);

        bindViews();

        if (isBlank(technicianId)) {
            Toast.makeText(this, "Data teknisi tidak valid.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupActions();
        renderFallback();
        loadTechnicianDetail();
    }

    private void bindViews() {
        btnBack = findViewById(R.id.btnBack);
        txtTechnicianInitial = findViewById(R.id.txtTechnicianInitial);
        txtTechnicianName = findViewById(R.id.txtTechnicianName);
        txtTechnicianStatus = findViewById(R.id.txtTechnicianStatus);
        txtRating = findViewById(R.id.txtRating);
        txtJobs = findViewById(R.id.txtJobs);
        txtReviews = findViewById(R.id.txtReviews);
        txtDescription = findViewById(R.id.txtDescription);
        layoutSupportedCategories = findViewById(R.id.layoutSupportedCategories);
        txtDetailMessage = findViewById(R.id.txtDetailMessage);
        btnOrderTechnician = findViewById(R.id.btnOrderTechnician);
    }

    private void setupActions() {
        BackButtonHelper.setup(btnBack, this::finish);

        btnOrderTechnician.setOnClickListener(v -> openOrderTechnician());
    }

    private void renderFallback() {
        String safeName = isBlank(technicianName) ? "Teknisi" : technicianName.trim();

        txtTechnicianInitial.setText(getInitial(safeName));
        txtTechnicianName.setText(safeName);
        txtTechnicianStatus.setText("Memuat...");
        txtRating.setText("★ 0.0\nRating");
        txtJobs.setText("0\nPekerjaan");
        txtReviews.setText("0\nUlasan");
        txtDescription.setText("Memuat data teknisi...");
        txtDetailMessage.setText("");
        setOrderEnabled(false);
    }

    private void loadTechnicianDetail() {
        ApiClient.getApiService(this)
                .getTechnicianDetail(technicianId)
                .enqueue(new Callback<ApiResponse<CustomerTechnicianResponse>>() {
                    @Override
                    public void onResponse(
                            Call<ApiResponse<CustomerTechnicianResponse>> call,
                            Response<ApiResponse<CustomerTechnicianResponse>> response
                    ) {
                        if (!response.isSuccessful()) {
                            showError(ErrorParser.parseError(response, "Detail teknisi gagal dimuat."));
                            return;
                        }

                        ApiResponse<CustomerTechnicianResponse> body = response.body();

                        if (body == null || !body.success || body.data == null) {
                            showError(ErrorParser.getBestMessage(body, "Detail teknisi gagal dimuat."));
                            return;
                        }

                        technicianDetail = body.data;
                        renderTechnician(technicianDetail);
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<CustomerTechnicianResponse>> call, Throwable t) {
                        showError("Tidak bisa terhubung ke server.");
                    }
                });
    }

    private void renderTechnician(CustomerTechnicianResponse technician) {
        String name = isBlank(technician.name) ? "Teknisi" : technician.name.trim();

        txtTechnicianInitial.setText(getInitial(name));
        txtTechnicianName.setText(name);
        txtTechnicianStatus.setText(getStatusText(technician.availabilityStatus));
        txtRating.setText("★ " + formatRating(technician.averageRating) + "\nRating");
        txtJobs.setText(formatNumber(technician.totalJobs) + "\nPekerjaan");
        txtReviews.setText(formatNumber(technician.ratingCount) + "\nUlasan");

        if (isBlank(technician.description)) {
            txtDescription.setText("Teknisi ini belum menambahkan deskripsi profil.");
        } else {
            txtDescription.setText(technician.description.trim());
        }

        renderSupportedCategories(technician.supportedDeviceCategories);

        boolean canOrder = hasSelectableCategory();
        setOrderEnabled(canOrder);

        txtDetailMessage.setText(canOrder
                ? "Pilih Pesan Teknisi untuk melanjutkan pemesanan."
                : "Teknisi ini belum memiliki kategori layanan aktif."
        );
    }

    private void renderSupportedCategories(List<DeviceCategoryResponse> categories) {
        layoutSupportedCategories.removeAllViews();

        if (categories == null || categories.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("Belum ada kategori layanan.");
            empty.setTextColor(Color.parseColor("#6B7680"));
            empty.setTextSize(14);
            layoutSupportedCategories.addView(empty);
            return;
        }

        for (DeviceCategoryResponse category : categories) {
            layoutSupportedCategories.addView(createCategoryRow(category));
        }
    }

    private LinearLayout createCategoryRow(DeviceCategoryResponse category) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(8), 0, dp(8));

        TextView icon = new TextView(this);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(38), dp(38));
        icon.setLayoutParams(iconParams);
        icon.setGravity(Gravity.CENTER);
        icon.setBackground(makeOval("#EAF4FF"));
        icon.setText("✓");
        icon.setTextColor(Color.parseColor("#2F4A8A"));
        icon.setTextSize(18);
        icon.setTypeface(Typeface.DEFAULT_BOLD);

        TextView name = new TextView(this);
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
        );
        nameParams.setMargins(dp(12), 0, 0, 0);
        name.setLayoutParams(nameParams);
        name.setText(category == null || isBlank(category.name) ? "Device" : getCleanCategoryName(category.name));
        name.setTextColor(Color.parseColor("#1F2329"));
        name.setTextSize(15);
        name.setTypeface(Typeface.DEFAULT_BOLD);
        name.setSingleLine(true);
        name.setEllipsize(TextUtils.TruncateAt.END);

        row.addView(icon);
        row.addView(name);

        return row;
    }

    private void openOrderTechnician() {
        if (!hasSelectableCategory()) {
            Toast.makeText(this, "Kategori layanan teknisi tidak tersedia.", Toast.LENGTH_SHORT).show();
            return;
        }

        DeviceCategoryResponse selectedCategory = getDefaultCategory();

        if (selectedCategory == null || isBlank(selectedCategory.deviceCategoryId)) {
            Toast.makeText(this, "Kategori layanan teknisi tidak valid.", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(TechnicianDetailActivity.this, OrderTechnicianActivity.class);
        intent.putExtra(OrderTechnicianActivity.EXTRA_CATEGORY_ID, selectedCategory.deviceCategoryId);
        intent.putExtra(OrderTechnicianActivity.EXTRA_CATEGORY_NAME, selectedCategory.name);
        intent.putExtra(OrderTechnicianActivity.EXTRA_TECHNICIAN_ID, technicianDetail.technicianProfileId);
        intent.putExtra(OrderTechnicianActivity.EXTRA_TECHNICIAN_NAME, technicianDetail.name);
        startActivity(intent);
    }

    private DeviceCategoryResponse getDefaultCategory() {
        if (technicianDetail == null
                || technicianDetail.supportedDeviceCategories == null
                || technicianDetail.supportedDeviceCategories.isEmpty()) {
            return null;
        }

        if (!isBlank(categoryId)) {
            for (DeviceCategoryResponse category : technicianDetail.supportedDeviceCategories) {
                if (category != null
                        && !isBlank(category.deviceCategoryId)
                        && category.deviceCategoryId.equals(categoryId)) {
                    return category;
                }
            }
        }

        for (DeviceCategoryResponse category : technicianDetail.supportedDeviceCategories) {
            if (category != null && !isBlank(category.deviceCategoryId)) {
                return category;
            }
        }

        return null;
    }

    private boolean hasSelectableCategory() {
        return getDefaultCategory() != null;
    }

    private void setOrderEnabled(boolean enabled) {
        btnOrderTechnician.setEnabled(enabled);
        btnOrderTechnician.setAlpha(enabled ? 1f : 0.55f);
    }

    private void showError(String message) {
        txtDetailMessage.setText(message);
        txtDescription.setText(message);
        setOrderEnabled(false);
    }

    private String getStatusText(String status) {
        if (isBlank(status)) {
            return "Tersedia";
        }

        String normalized = status.trim().replace("_", " ").toLowerCase();

        if (normalized.contains("available") || normalized.contains("online")) {
            return "Tersedia";
        }

        if (normalized.contains("busy")) {
            return "Busy";
        }

        if (normalized.contains("offline")) {
            return "Offline";
        }

        return status.trim();
    }

    private String formatRating(BigDecimal rating) {
        if (rating == null) {
            return "0.0";
        }

        return rating.setScale(1, java.math.RoundingMode.HALF_UP).toPlainString();
    }

    private String formatNumber(Integer value) {
        return String.valueOf(value == null ? 0 : value);
    }

    private String getInitial(String name) {
        if (isBlank(name)) {
            return "T";
        }

        String[] parts = name.trim().split("\\s+");

        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }

        return (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase();
    }

    private String getCleanCategoryName(String name) {
        if (isBlank(name)) {
            return "Perangkat";
        }

        String lower = name.toLowerCase();

        if (lower.contains("air conditioner")) {
            return "AC";
        }

        if (lower.contains("refrigerator")) {
            return "Fridge";
        }

        if (lower.contains("television")) {
            return "TV";
        }

        return name.trim();
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

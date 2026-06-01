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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.teknisio.mobile.R;
import com.teknisio.mobile.model.response.ApiResponse;
import com.teknisio.mobile.model.response.CustomerTechnicianResponse;
import com.teknisio.mobile.model.response.DeviceCategoryResponse;
import com.teknisio.mobile.network.ApiClient;
import com.teknisio.mobile.util.BackButtonHelper;
import com.teknisio.mobile.util.ErrorParser;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TechnicianListActivity extends AppCompatActivity {

    public static final String EXTRA_CATEGORY_ID = "extra_category_id";
    public static final String EXTRA_CATEGORY_NAME = "extra_category_name";

    private FrameLayout btnBack;
    private ImageView imgCategoryIcon;
    private TextView txtCategoryName;
    private TextView txtTechnicianCount;
    private LinearLayout layoutTechnicians;
    private TextView txtTechnicianEmpty;
    private Button btnMeetTechnician;

    private String categoryId;
    private String categoryName;

    private final List<CustomerTechnicianResponse> technicians = new ArrayList<>();
    private CustomerTechnicianResponse selectedTechnician;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_technician_list);

        categoryId = getIntent().getStringExtra(EXTRA_CATEGORY_ID);
        categoryName = getIntent().getStringExtra(EXTRA_CATEGORY_NAME);

        bindViews();
        setupInitialData();
        setupActions();
        loadTechnicians();
    }

    private void bindViews() {
        btnBack = findViewById(R.id.btnBack);
        imgCategoryIcon = findViewById(R.id.imgCategoryIcon);
        txtCategoryName = findViewById(R.id.txtCategoryName);
        txtTechnicianCount = findViewById(R.id.txtTechnicianCount);
        layoutTechnicians = findViewById(R.id.layoutTechnicians);
        txtTechnicianEmpty = findViewById(R.id.txtTechnicianEmpty);
        btnMeetTechnician = findViewById(R.id.btnMeetTechnician);
    }

    private void setupInitialData() {
        if (isBlank(categoryId)) {
            Toast.makeText(this, "Kategori tidak valid.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        txtCategoryName.setText(getCleanCategoryName(categoryName));
        imgCategoryIcon.setImageResource(getCategoryIconResId(categoryName));
        setLoading(false);
    }

    private void setupActions() {
        BackButtonHelper.setup(btnBack, this::finish);

        btnMeetTechnician.setOnClickListener(v -> {
            if (selectedTechnician == null || isBlank(selectedTechnician.technicianProfileId)) {
                Toast.makeText(this, "Pilih teknisi terlebih dahulu.", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(TechnicianListActivity.this, OrderTechnicianActivity.class);
            intent.putExtra(OrderTechnicianActivity.EXTRA_CATEGORY_ID, categoryId);
            intent.putExtra(OrderTechnicianActivity.EXTRA_CATEGORY_NAME, categoryName);
            intent.putExtra(OrderTechnicianActivity.EXTRA_TECHNICIAN_ID, selectedTechnician.technicianProfileId);
            intent.putExtra(OrderTechnicianActivity.EXTRA_TECHNICIAN_NAME, selectedTechnician.name);
            startActivity(intent);
        });
    }

    private void loadTechnicians() {
        showMessage("Memuat teknisi spesialis " + getCleanCategoryName(categoryName) + "...");
        setLoading(true);

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
                            showMessage(ErrorParser.parseError(response, "Teknisi gagal dimuat."));
                            return;
                        }

                        ApiResponse<List<CustomerTechnicianResponse>> body = response.body();

                        if (body == null || !body.success) {
                            showMessage(ErrorParser.getBestMessage(body, "Teknisi gagal dimuat."));
                            return;
                        }

                        technicians.clear();

                        if (body.data != null) {
                            technicians.addAll(body.data);
                        }

                        if (technicians.isEmpty()) {
                            selectedTechnician = null;
                            showMessage("Belum ada teknisi untuk kategori " + getCleanCategoryName(categoryName) + ".");
                            setLoading(false);
                            return;
                        }

                        selectedTechnician = technicians.get(0);
                        txtTechnicianEmpty.setVisibility(android.view.View.GONE);
                        txtTechnicianCount.setText(technicians.size() + " teknisi tersedia");
                        renderTechnicians();
                        setLoading(false);
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<CustomerTechnicianResponse>>> call, Throwable t) {
                        setLoading(false);
                        showMessage("Tidak bisa terhubung ke server.");
                    }
                });
    }

    private void renderTechnicians() {
        layoutTechnicians.removeAllViews();

        for (CustomerTechnicianResponse technician : technicians) {
            layoutTechnicians.addView(createTechnicianCard(technician));
        }
    }

    private LinearLayout createTechnicianCard(CustomerTechnicianResponse technician) {
        boolean selected = isSelected(technician);

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14), dp(14), dp(14), dp(14));
        card.setBackground(makeStrokeRounded("#FFFFFF", selected ? "#2F4A8A" : "#DCE6EB", 18, selected ? 2 : 1));
        card.setElevation(dp(selected ? 4 : 1));

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, dp(12));
        card.setLayoutParams(cardParams);

        LinearLayout topRow = new LinearLayout(this);
        topRow.setGravity(Gravity.CENTER_VERTICAL);
        topRow.setOrientation(LinearLayout.HORIZONTAL);

        TextView avatar = new TextView(this);
        avatar.setLayoutParams(new LinearLayout.LayoutParams(dp(56), dp(56)));
        avatar.setGravity(Gravity.CENTER);
        avatar.setBackground(makeOval("#EAF4FF"));
        avatar.setText(getInitial(technician == null ? null : technician.name));
        avatar.setTextColor(Color.parseColor("#2F4A8A"));
        avatar.setTextSize(20);
        avatar.setTypeface(Typeface.DEFAULT_BOLD);

        LinearLayout info = new LinearLayout(this);
        LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        infoParams.setMargins(dp(12), 0, dp(10), 0);
        info.setLayoutParams(infoParams);
        info.setOrientation(LinearLayout.VERTICAL);

        TextView name = new TextView(this);
        name.setText(technician == null || isBlank(technician.name) ? "Teknisi" : technician.name);
        name.setTextColor(Color.parseColor("#1F2329"));
        name.setTextSize(17);
        name.setTypeface(Typeface.DEFAULT_BOLD);
        name.setSingleLine(true);
        name.setEllipsize(TextUtils.TruncateAt.END);

        TextView skill = new TextView(this);
        skill.setText(getSkillText(technician));
        skill.setTextColor(Color.parseColor("#6B7680"));
        skill.setTextSize(13);
        skill.setMaxLines(2);
        skill.setEllipsize(TextUtils.TruncateAt.END);

        info.addView(name);
        info.addView(skill);

        TextView check = new TextView(this);
        check.setLayoutParams(new LinearLayout.LayoutParams(dp(34), dp(34)));
        check.setGravity(Gravity.CENTER);
        check.setText(selected ? "✓" : "");
        check.setTextColor(Color.WHITE);
        check.setTextSize(20);
        check.setTypeface(Typeface.DEFAULT_BOLD);
        check.setBackground(selected
                ? makeOval("#2F4A8A")
                : makeStrokeOval("#FFFFFF", "#D4DEE5", 1)
        );

        topRow.addView(avatar);
        topRow.addView(info);
        topRow.addView(check);

        LinearLayout metaRow = new LinearLayout(this);
        LinearLayout.LayoutParams metaParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        metaParams.setMargins(0, dp(14), 0, 0);
        metaRow.setLayoutParams(metaParams);
        metaRow.setGravity(Gravity.CENTER_VERTICAL);
        metaRow.setOrientation(LinearLayout.HORIZONTAL);

        metaRow.addView(createChip("★ " + formatRating(technician == null ? null : technician.averageRating), "#FFF8E1", "#E8D98B", "#8A6D00"));
        metaRow.addView(createChip(formatJobs(technician == null ? null : technician.totalJobs), "#F8FBFC", "#DCE6EB", "#5F6B73"));
        metaRow.addView(createChip(getStatusText(technician == null ? null : technician.availabilityStatus), "#EDF9F0", "#CFE9D6", "#2E7D32"));

        card.addView(topRow);
        card.addView(metaRow);

        card.setOnClickListener(v -> {
            selectedTechnician = technician;
            renderTechnicians();
            setLoading(false);
        });

        return card;
    }

    private TextView createChip(String text, String fill, String stroke, String textColor) {
        TextView chip = new TextView(this);
        chip.setText(text);
        chip.setTextSize(11);
        chip.setTypeface(Typeface.DEFAULT_BOLD);
        chip.setTextColor(Color.parseColor(textColor));
        chip.setGravity(Gravity.CENTER);
        chip.setSingleLine(true);
        chip.setPadding(dp(9), dp(5), dp(9), dp(5));
        chip.setBackground(makeStrokeRounded(fill, stroke, 14, 1));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                dp(30)
        );
        params.setMargins(0, 0, dp(8), 0);
        chip.setLayoutParams(params);

        return chip;
    }

    private void showMessage(String message) {
        layoutTechnicians.removeAllViews();
        txtTechnicianEmpty.setVisibility(android.view.View.VISIBLE);
        txtTechnicianEmpty.setText(message);
        txtTechnicianCount.setText(message);
    }

    private void setLoading(boolean loading) {
        btnMeetTechnician.setEnabled(!loading && selectedTechnician != null);
        btnMeetTechnician.setAlpha((!loading && selectedTechnician != null) ? 1f : 0.55f);
        btnMeetTechnician.setText(loading ? "Loading..." : "Meet Technician");
    }

    private boolean isSelected(CustomerTechnicianResponse technician) {
        return technician != null
                && selectedTechnician != null
                && !isBlank(technician.technicianProfileId)
                && technician.technicianProfileId.equals(selectedTechnician.technicianProfileId);
    }

    private String getSkillText(CustomerTechnicianResponse technician) {
        if (technician == null) {
            return "Teknisi terdaftar";
        }

        if (technician.supportedDeviceCategories != null && !technician.supportedDeviceCategories.isEmpty()) {
            List<String> names = new ArrayList<>();

            for (DeviceCategoryResponse category : technician.supportedDeviceCategories) {
                if (category != null && !isBlank(category.name)) {
                    names.add(getCleanCategoryName(category.name));
                }
            }

            if (!names.isEmpty()) {
                return TextUtils.join(" • ", names);
            }
        }

        if (!isBlank(technician.description)) {
            return technician.description.trim();
        }

        return "Teknisi terdaftar";
    }

    private String getStatusText(String status) {
        if (isBlank(status)) {
            return "Available";
        }

        String normalized = status.trim().replace("_", " ").toLowerCase();

        if (normalized.contains("available") || normalized.contains("online")) {
            return "Available";
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

    private String formatJobs(Integer totalJobs) {
        return (totalJobs == null ? 0 : totalJobs) + " jobs";
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

    private GradientDrawable makeStrokeOval(String fillColor, String strokeColor, int strokeWidthDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setColor(Color.parseColor(fillColor));
        drawable.setStroke(dp(strokeWidthDp), Color.parseColor(strokeColor));
        return drawable;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}

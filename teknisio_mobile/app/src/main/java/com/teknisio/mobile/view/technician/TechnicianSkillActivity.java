package com.teknisio.mobile.view.technician;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.teknisio.mobile.R;
import com.teknisio.mobile.base.BaseActivity;
import com.teknisio.mobile.model.request.AddTechnicianDeviceCategoryRequest;
import com.teknisio.mobile.model.response.ApiResponse;
import com.teknisio.mobile.model.response.DeviceCategoryResponse;
import com.teknisio.mobile.network.ApiClient;
import com.teknisio.mobile.util.AppToast;
import com.teknisio.mobile.util.BackButtonHelper;
import com.teknisio.mobile.util.ErrorParser;
import com.teknisio.mobile.util.TextHelper;
import com.teknisio.mobile.util.ViewHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TechnicianSkillActivity extends BaseActivity {

    private FrameLayout btnBack;
    private TextView txtSkillSubtitle;
    private LinearLayout layoutCurrentSkills;
    private LinearLayout layoutAvailableSkills;
    private TextView txtCurrentSkillEmpty;
    private TextView txtAvailableSkillEmpty;
    private Button btnRefreshSkills;

    private final List<DeviceCategoryResponse> allCategories = new ArrayList<>();
    private final List<DeviceCategoryResponse> mySkills = new ArrayList<>();

    private boolean loading = false;
    private boolean mutating = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_technician_skills);

        bindViews();
        setupActions();
        loadSkills();
    }

    private void bindViews() {
        btnBack = findViewById(R.id.btnBack);
        txtSkillSubtitle = findViewById(R.id.txtSkillSubtitle);
        layoutCurrentSkills = findViewById(R.id.layoutCurrentSkills);
        layoutAvailableSkills = findViewById(R.id.layoutAvailableSkills);
        txtCurrentSkillEmpty = findViewById(R.id.txtCurrentSkillEmpty);
        txtAvailableSkillEmpty = findViewById(R.id.txtAvailableSkillEmpty);
        btnRefreshSkills = findViewById(R.id.btnRefreshSkills);
    }

    private void setupActions() {
        BackButtonHelper.setup(btnBack, this::finish);
        btnRefreshSkills.setOnClickListener(v -> loadSkills());
    }

    private void loadSkills() {
        if (loading) {
            return;
        }

        loading = true;
        setLoadingState("Memuat keahlian teknisi...");

        ApiClient.getApiService(this)
                .getDeviceCategories()
                .enqueue(new Callback<ApiResponse<List<DeviceCategoryResponse>>>() {
                    @Override
                    public void onResponse(
                            Call<ApiResponse<List<DeviceCategoryResponse>>> call,
                            Response<ApiResponse<List<DeviceCategoryResponse>>> response
                    ) {
                        if (!response.isSuccessful()) {
                            loading = false;
                            showError(ErrorParser.parseError(response, "Kategori perangkat gagal dimuat."));
                            return;
                        }

                        ApiResponse<List<DeviceCategoryResponse>> body = response.body();

                        if (body == null || !body.success) {
                            loading = false;
                            showError(ErrorParser.getBestMessage(body, "Kategori perangkat gagal dimuat."));
                            return;
                        }

                        allCategories.clear();

                        if (body.data != null) {
                            allCategories.addAll(body.data);
                        }

                        loadMySkills();
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<DeviceCategoryResponse>>> call, Throwable t) {
                        loading = false;
                        showError("Tidak bisa terhubung ke server.");
                    }
                });
    }

    private void loadMySkills() {
        ApiClient.getApiService(this)
                .getTechnicianDeviceCategories()
                .enqueue(new Callback<ApiResponse<List<DeviceCategoryResponse>>>() {
                    @Override
                    public void onResponse(
                            Call<ApiResponse<List<DeviceCategoryResponse>>> call,
                            Response<ApiResponse<List<DeviceCategoryResponse>>> response
                    ) {
                        loading = false;

                        if (!response.isSuccessful()) {
                            showError(ErrorParser.parseError(response, "Keahlian teknisi gagal dimuat."));
                            return;
                        }

                        ApiResponse<List<DeviceCategoryResponse>> body = response.body();

                        if (body == null || !body.success) {
                            showError(ErrorParser.getBestMessage(body, "Keahlian teknisi gagal dimuat."));
                            return;
                        }

                        mySkills.clear();

                        if (body.data != null) {
                            mySkills.addAll(body.data);
                        }

                        renderSkills();
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<DeviceCategoryResponse>>> call, Throwable t) {
                        loading = false;
                        showError("Tidak bisa terhubung ke server.");
                    }
                });
    }

    private void renderSkills() {
        layoutCurrentSkills.removeAllViews();
        layoutAvailableSkills.removeAllViews();

        Set<String> ownedIds = getOwnedSkillIds();

        for (DeviceCategoryResponse skill : mySkills) {
            if (skill != null && !TextHelper.isBlank(skill.deviceCategoryId)) {
                layoutCurrentSkills.addView(createSkillRow(skill, true));
            }
        }

        for (DeviceCategoryResponse category : allCategories) {
            if (category == null || TextHelper.isBlank(category.deviceCategoryId)) {
                continue;
            }

            if (!ownedIds.contains(category.deviceCategoryId)) {
                layoutAvailableSkills.addView(createSkillRow(category, false));
            }
        }

        boolean currentEmpty = layoutCurrentSkills.getChildCount() == 0;
        boolean availableEmpty = layoutAvailableSkills.getChildCount() == 0;

        txtCurrentSkillEmpty.setVisibility(currentEmpty ? View.VISIBLE : View.GONE);
        txtAvailableSkillEmpty.setVisibility(availableEmpty ? View.VISIBLE : View.GONE);

        txtSkillSubtitle.setText(mySkills.size() + " keahlian aktif dari " + allCategories.size() + " kategori.");
        btnRefreshSkills.setText("Refresh");
        btnRefreshSkills.setEnabled(true);
    }

    private LinearLayout createSkillRow(DeviceCategoryResponse category, boolean owned) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(8), 0, dp(8));

        TextView icon = new TextView(this);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(40), dp(40));
        icon.setLayoutParams(iconParams);
        icon.setGravity(Gravity.CENTER);
        icon.setBackground(ViewHelper.oval(owned ? "#EAF4FF" : "#F3F7F9"));
        icon.setText(owned ? "✓" : "+");
        icon.setTextColor(Color.parseColor("#2F4A8A"));
        icon.setTextSize(18);
        icon.setTypeface(Typeface.DEFAULT_BOLD);

        LinearLayout textContainer = new LinearLayout(this);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
        );
        textParams.setMargins(dp(12), 0, dp(10), 0);
        textContainer.setLayoutParams(textParams);
        textContainer.setOrientation(LinearLayout.VERTICAL);

        TextView name = new TextView(this);
        name.setText(TextHelper.safe(category == null ? null : category.name, "Device"));
        name.setTextColor(Color.parseColor("#1F2329"));
        name.setTextSize(15);
        name.setTypeface(Typeface.DEFAULT_BOLD);
        name.setSingleLine(true);
        name.setEllipsize(TextUtils.TruncateAt.END);

        TextView desc = new TextView(this);
        desc.setText(owned ? "Aktif sebagai keahlian kamu" : "Belum ditambahkan");
        desc.setTextColor(Color.parseColor("#6B7680"));
        desc.setTextSize(12);

        textContainer.addView(name);
        textContainer.addView(desc);

        Button action = new Button(this);
        LinearLayout.LayoutParams actionParams = new LinearLayout.LayoutParams(dp(92), dp(42));
        action.setLayoutParams(actionParams);
        action.setAllCaps(false);
        action.setText(owned ? "Remove" : "Add");
        action.setTextSize(12);
        action.setTextColor(owned ? Color.parseColor("#C62828") : Color.WHITE);
        action.setBackgroundResource(owned ? R.drawable.bg_dialog_secondary_button : R.drawable.bg_order_primary);
        action.setEnabled(!mutating);

        if (owned) {
            action.setOnClickListener(v -> removeSkill(category));
        } else {
            action.setOnClickListener(v -> addSkill(category));
        }

        row.addView(icon);
        row.addView(textContainer);
        row.addView(action);

        return row;
    }

    private void addSkill(DeviceCategoryResponse category) {
        if (mutating || category == null || TextHelper.isBlank(category.deviceCategoryId)) {
            return;
        }

        mutating = true;
        setLoadingState("Menambahkan keahlian...");

        ApiClient.getApiService(this)
                .addTechnicianDeviceCategory(
                        new AddTechnicianDeviceCategoryRequest(category.deviceCategoryId)
                )
                .enqueue(new Callback<ApiResponse<DeviceCategoryResponse>>() {
                    @Override
                    public void onResponse(
                            Call<ApiResponse<DeviceCategoryResponse>> call,
                            Response<ApiResponse<DeviceCategoryResponse>> response
                    ) {
                        mutating = false;

                        if (!response.isSuccessful()) {
                            AppToast.error(
                                    TechnicianSkillActivity.this,
                                    ErrorParser.parseError(response, "Keahlian gagal ditambahkan.")
                            );
                            renderSkills();
                            return;
                        }

                        AppToast.success(TechnicianSkillActivity.this, "Keahlian berhasil ditambahkan.");
                        loadSkills();
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<DeviceCategoryResponse>> call, Throwable t) {
                        mutating = false;
                        AppToast.error(TechnicianSkillActivity.this, "Tidak bisa terhubung ke server.");
                        renderSkills();
                    }
                });
    }

    private void removeSkill(DeviceCategoryResponse category) {
        if (mutating || category == null || TextHelper.isBlank(category.deviceCategoryId)) {
            return;
        }

        mutating = true;
        setLoadingState("Menghapus keahlian...");

        ApiClient.getApiService(this)
                .deleteTechnicianDeviceCategory(category.deviceCategoryId)
                .enqueue(new Callback<ApiResponse<Object>>() {
                    @Override
                    public void onResponse(
                            Call<ApiResponse<Object>> call,
                            Response<ApiResponse<Object>> response
                    ) {
                        mutating = false;

                        if (!response.isSuccessful()) {
                            AppToast.error(
                                    TechnicianSkillActivity.this,
                                    ErrorParser.parseError(response, "Keahlian gagal dihapus.")
                            );
                            renderSkills();
                            return;
                        }

                        AppToast.success(TechnicianSkillActivity.this, "Keahlian berhasil dihapus.");
                        loadSkills();
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                        mutating = false;
                        AppToast.error(TechnicianSkillActivity.this, "Tidak bisa terhubung ke server.");
                        renderSkills();
                    }
                });
    }

    private Set<String> getOwnedSkillIds() {
        Set<String> ids = new HashSet<>();

        for (DeviceCategoryResponse skill : mySkills) {
            if (skill != null && !TextHelper.isBlank(skill.deviceCategoryId)) {
                ids.add(skill.deviceCategoryId);
            }
        }

        return ids;
    }

    private void setLoadingState(String message) {
        txtSkillSubtitle.setText(message);
        btnRefreshSkills.setText("Memuat...");
        btnRefreshSkills.setEnabled(false);
    }

    private void showError(String message) {
        layoutCurrentSkills.removeAllViews();
        layoutAvailableSkills.removeAllViews();
        txtCurrentSkillEmpty.setVisibility(View.VISIBLE);
        txtAvailableSkillEmpty.setVisibility(View.GONE);
        txtCurrentSkillEmpty.setText(message);
        txtSkillSubtitle.setText(message);
        btnRefreshSkills.setText("Refresh");
        btnRefreshSkills.setEnabled(true);
    }

    private int dp(int value) {
        return ViewHelper.dp(this, value);
    }
}

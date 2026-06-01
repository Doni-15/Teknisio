package com.teknisio.mobile.view.customer;

import com.teknisio.mobile.base.BaseActivity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.os.Looper;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.teknisio.mobile.R;
import com.teknisio.mobile.local.TokenManager;
import com.teknisio.mobile.model.response.ApiResponse;
import com.teknisio.mobile.model.response.CustomerTechnicianResponse;
import com.teknisio.mobile.model.response.DeviceCategoryResponse;
import com.teknisio.mobile.network.ApiClient;
import com.teknisio.mobile.view.auth.LoginActivity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomerHomeActivity extends BaseActivity {

    private TextView txtAvatarInitial;
    private TextView txtHomeName;
    private TextView txtHomeAddress;
    private GridLayout gridCategories;
    private TextView txtCategoryEmpty;
    private LinearLayout layoutTechnicians;
    private TextView txtTechnicianEmpty;
    private HorizontalScrollView scrollTechnicians;
    private FrameLayout btnNotification;

    private LinearLayout navChat;
    private LinearLayout navHistory;
    private LinearLayout navAccount;

    private TokenManager tokenManager;

    private final Handler technicianCarouselHandler = new Handler(Looper.getMainLooper());
    private final Runnable snapTechnicianRunnable = this::snapTechnicianCarousel;

    private final List<DeviceCategoryResponse> categories = new ArrayList<>();
    private final List<CustomerTechnicianResponse> allTechnicians = new ArrayList<>();
    private final List<CustomerTechnicianResponse> carouselTechnicians = new ArrayList<>();
    private String selectedCategoryId;
    private int realTechnicianCount = 0;
    private int carouselPosition = 0;
    private boolean technicianCarouselReady = false;
    private boolean adjustingTechnicianCarousel = false;
    private int pendingTechnicianRequests = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_home);

        tokenManager = new TokenManager(this);

        bindViews();
        setupHeader();
        setupNavigation();
        setupTechnicianCarouselScroll();
        loadDeviceCategories();
    }

    private void bindViews() {
        txtAvatarInitial = findViewById(R.id.txtAvatarInitial);
        txtHomeName = findViewById(R.id.txtHomeName);
        txtHomeAddress = findViewById(R.id.txtHomeAddress);
        gridCategories = findViewById(R.id.gridCategories);
        txtCategoryEmpty = findViewById(R.id.txtCategoryEmpty);
        layoutTechnicians = findViewById(R.id.layoutTechnicians);
        txtTechnicianEmpty = findViewById(R.id.txtTechnicianEmpty);
        scrollTechnicians = findViewById(R.id.scrollTechnicians);
        btnNotification = findViewById(R.id.btnNotification);

        navChat = findViewById(R.id.navChat);
        navHistory = findViewById(R.id.navHistory);
        navAccount = findViewById(R.id.navAccount);
    }

    private void setupHeader() {
        String name = tokenManager.getName();
        String address = tokenManager.getAddress();

        txtAvatarInitial.setText(getInitial(name));
        txtHomeName.setText(getDisplayName(name));

        if (address == null || address.trim().isEmpty()) {
            txtHomeAddress.setText("Alamat belum diatur");
        } else {
            txtHomeAddress.setText(address.trim());
        }
    }

    private void setupNavigation() {
        if (btnNotification != null) {
            btnNotification.setOnClickListener(v -> {
                Intent intent = new Intent(CustomerHomeActivity.this, NotificationActivity.class);
                startActivity(intent);
            });
        }

        navChat.setOnClickListener(v ->
                Toast.makeText(this, "Chat belum tersedia.", Toast.LENGTH_SHORT).show()
        );

        navHistory.setOnClickListener(v -> {
            Intent intent = new Intent(CustomerHomeActivity.this, OrderHistoryActivity.class);
            startActivity(intent);
        });

        navAccount.setOnClickListener(v -> {
            Intent intent = new Intent(CustomerHomeActivity.this, AccountActivity.class);
            startActivity(intent);
        });

        navAccount.setOnLongClickListener(v -> {
            logout();
            return true;
        });
    }

    private void loadDeviceCategories() {
        showCategoryMessage("Memuat kategori...");

        ApiClient.getApiService(this)
                .getDeviceCategories()
                .enqueue(new Callback<ApiResponse<List<DeviceCategoryResponse>>>() {
                    @Override
                    public void onResponse(
                            Call<ApiResponse<List<DeviceCategoryResponse>>> call,
                            Response<ApiResponse<List<DeviceCategoryResponse>>> response
                    ) {
                        if (!response.isSuccessful() || response.body() == null || !response.body().success) {
                            showCategoryMessage("Kategori gagal dimuat.");
                            showTechnicianMessage("Teknisi belum bisa dimuat karena kategori gagal dimuat.");
                            return;
                        }

                        List<DeviceCategoryResponse> data = response.body().data;

                        categories.clear();

                        if (data != null) {
                            categories.addAll(data);
                        }

                        if (categories.isEmpty()) {
                            showCategoryMessage("Belum ada kategori perangkat.");
                            showTechnicianMessage("Belum ada teknisi yang bisa ditampilkan.");
                            return;
                        }

                        selectedCategoryId = null;
                        renderCategories();

                        loadAllTechnicians();
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<DeviceCategoryResponse>>> call, Throwable t) {
                        showCategoryMessage("Tidak bisa terhubung ke server.");
                        showTechnicianMessage("Teknisi belum bisa dimuat.");
                    }
                });
    }

    private void loadAllTechnicians() {
        allTechnicians.clear();

        if (categories.isEmpty()) {
            showTechnicianMessage("Belum ada kategori untuk memuat teknisi.");
            return;
        }

        showTechnicianMessage("Memuat semua teknisi...");
        pendingTechnicianRequests = categories.size();

        for (DeviceCategoryResponse category : categories) {
            if (category == null
                    || category.deviceCategoryId == null
                    || category.deviceCategoryId.trim().isEmpty()) {
                finishTechnicianBatchRequest();
                continue;
            }

            ApiClient.getApiService(this)
                    .searchTechnicians(category.deviceCategoryId, null, "rating")
                    .enqueue(new Callback<ApiResponse<List<CustomerTechnicianResponse>>>() {
                        @Override
                        public void onResponse(
                                Call<ApiResponse<List<CustomerTechnicianResponse>>> call,
                                Response<ApiResponse<List<CustomerTechnicianResponse>>> response
                        ) {
                            if (response.isSuccessful()
                                    && response.body() != null
                                    && response.body().success
                                    && response.body().data != null) {
                                for (CustomerTechnicianResponse technician : response.body().data) {
                                    addUniqueTechnician(technician);
                                }
                            }

                            finishTechnicianBatchRequest();
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<List<CustomerTechnicianResponse>>> call, Throwable t) {
                            finishTechnicianBatchRequest();
                        }
                    });
        }
    }

    private void finishTechnicianBatchRequest() {
        pendingTechnicianRequests--;

        if (pendingTechnicianRequests > 0) {
            return;
        }

        if (allTechnicians.isEmpty()) {
            showTechnicianMessage("Belum ada teknisi terdaftar.");
            return;
        }

        renderTechnicians(allTechnicians);
    }

    private void addUniqueTechnician(CustomerTechnicianResponse technician) {
        if (technician == null) {
            return;
        }

        for (CustomerTechnicianResponse existing : allTechnicians) {
            if (isSameTechnician(existing, technician)) {
                return;
            }
        }

        allTechnicians.add(technician);
    }

    private boolean isSameTechnician(
            CustomerTechnicianResponse first,
            CustomerTechnicianResponse second
    ) {
        if (first == null || second == null) {
            return false;
        }

        if (!isBlank(first.technicianProfileId) && !isBlank(second.technicianProfileId)) {
            return first.technicianProfileId.equals(second.technicianProfileId);
        }

        if (!isBlank(first.name) && !isBlank(second.name)) {
            return first.name.equalsIgnoreCase(second.name);
        }

        return false;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private void renderCategories() {
        txtCategoryEmpty.setVisibility(android.view.View.GONE);
        gridCategories.setVisibility(android.view.View.VISIBLE);
        gridCategories.removeAllViews();

        int maxItems = Math.min(categories.size(), 8);

        for (int i = 0; i < maxItems; i++) {
            DeviceCategoryResponse category = categories.get(i);
            gridCategories.addView(createCategoryItem(category));
        }
    }

    private LinearLayout createCategoryItem(DeviceCategoryResponse category) {
        boolean selected = category != null
                && category.deviceCategoryId != null
                && category.deviceCategoryId.equals(selectedCategoryId);

        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.VERTICAL);
        item.setGravity(Gravity.CENTER);
        item.setPadding(dp(4), dp(8), dp(4), dp(8));

        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        params.setMargins(dp(2), dp(4), dp(2), dp(8));
        item.setLayoutParams(params);

        FrameLayout iconCircle = new FrameLayout(this);
        LinearLayout.LayoutParams iconCircleParams = new LinearLayout.LayoutParams(dp(54), dp(54));
        iconCircle.setLayoutParams(iconCircleParams);
        iconCircle.setBackground(makeOval(selected ? "#C8F1F5" : "#DDF8FA"));

        int iconResId = getCategoryIconResId(category == null ? null : category.name);

        if (iconResId != 0) {
            ImageView iconImage = new ImageView(this);

            FrameLayout.LayoutParams imageParams = new FrameLayout.LayoutParams(
                    dp(31),
                    dp(31),
                    Gravity.CENTER
            );

            iconImage.setLayoutParams(imageParams);
            iconImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
            iconImage.setImageResource(iconResId);

            iconCircle.addView(iconImage);
        } else {
            TextView fallbackIcon = new TextView(this);

            FrameLayout.LayoutParams fallbackParams = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            );

            fallbackIcon.setLayoutParams(fallbackParams);
            fallbackIcon.setGravity(Gravity.CENTER);
            fallbackIcon.setText(getCategoryShortName(category == null ? null : category.name));
            fallbackIcon.setTextColor(Color.parseColor("#2F4A8A"));
            fallbackIcon.setTextSize(13);
            fallbackIcon.setTypeface(Typeface.DEFAULT_BOLD);

            iconCircle.addView(fallbackIcon);
        }

        TextView name = new TextView(this);
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        nameParams.setMargins(0, dp(9), 0, 0);
        name.setLayoutParams(nameParams);
        name.setGravity(Gravity.CENTER);
        name.setText(getCategoryDisplayName(category == null ? null : category.name));
        name.setTextColor(selected ? Color.parseColor("#2F4A8A") : Color.parseColor("#1F2329"));
        name.setTextSize(13);
        name.setTypeface(selected ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        name.setMaxLines(2);
        name.setEllipsize(TextUtils.TruncateAt.END);

        item.addView(iconCircle);
        item.addView(name);

        item.setOnClickListener(v -> {
            if (category == null || category.deviceCategoryId == null) {
                return;
            }

            selectedCategoryId = category.deviceCategoryId;
            renderCategories();
            openOrderByCategory(category);
        });

        return item;
    }

    private String getCategoryDisplayName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "-";
        }

        String lower = name.toLowerCase().trim();

        if (lower.equals("ac")
                || lower.contains("air conditioner")
                || lower.contains("pendingin")) {
            return "AC";
        }

        if (lower.contains("washing") || lower.contains("wash") || lower.contains("cuci")) {
            return "Washing\nMachine";
        }

        if (lower.contains("rice") || lower.contains("nasi")) {
            return "Rice\nCooker";
        }

        if (lower.contains("fridge")
                || lower.contains("refrigerator")
                || lower.contains("kulkas")) {
            return "Fridge";
        }

        if (lower.contains("television") || lower.equals("tv") || lower.contains("televisi")) {
            return "TV";
        }

        if (lower.contains("oven")) {
            return "Oven";
        }

        if (lower.contains("fan") || lower.contains("kipas")) {
            return "Fan";
        }

        if (lower.contains("mixer")) {
            return "Mixer";
        }

        return name.trim();
    }

    private void openOrderByCategory(DeviceCategoryResponse category) {
        if (category == null
                || category.deviceCategoryId == null
                || category.deviceCategoryId.trim().isEmpty()) {
            Toast.makeText(this, "Kategori tidak valid.", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(CustomerHomeActivity.this, TechnicianListActivity.class);
        intent.putExtra(TechnicianListActivity.EXTRA_CATEGORY_ID, category.deviceCategoryId);
        intent.putExtra(
                TechnicianListActivity.EXTRA_CATEGORY_NAME,
                getCategoryDisplayName(category.name).replace("\n", " ")
        );
        startActivity(intent);
    }

    private int getCategoryIconResId(String name) {
        if (name == null || name.trim().isEmpty()) {
            return 0;
        }

        String lower = name.toLowerCase().trim();

        if (lower.contains("washing") || lower.contains("wash") || lower.contains("cuci")) {
            return R.drawable.washing_machine;
        }

        if (lower.contains("rice") || lower.contains("nasi")) {
            return R.drawable.rice_cooker;
        }

        if (lower.contains("fridge")
                || lower.contains("refrigerator")
                || lower.contains("kulkas")) {
            return R.drawable.refrigerator;
        }

        if (lower.contains("oven")) {
            return R.drawable.oven;
        }

        if (lower.contains("television") || lower.equals("tv") || lower.contains("televisi")) {
            return R.drawable.television;
        }

        if (lower.contains("fan") || lower.contains("kipas")) {
            return R.drawable.fan;
        }

        if (lower.contains("mixer")) {
            return R.drawable.mixer;
        }

        if (lower.equals("ac")
                || lower.contains("air conditioner")
                || lower.contains("pendingin")) {
            return R.drawable.ac;
        }

        return 0;
    }

    private void renderTechnicians(List<CustomerTechnicianResponse> technicians) {
        technicianCarouselReady = false;
        technicianCarouselHandler.removeCallbacks(snapTechnicianRunnable);

        txtTechnicianEmpty.setVisibility(android.view.View.GONE);
        scrollTechnicians.setVisibility(android.view.View.VISIBLE);
        layoutTechnicians.removeAllViews();
        carouselTechnicians.clear();

        if (technicians == null || technicians.isEmpty()) {
            showTechnicianMessage("Belum ada teknisi terdaftar.");
            return;
        }

        realTechnicianCount = technicians.size();

        int repeatCount = realTechnicianCount <= 1 ? 1 : 31;

        for (int repeat = 0; repeat < repeatCount; repeat++) {
            carouselTechnicians.addAll(technicians);
        }

        for (CustomerTechnicianResponse technician : carouselTechnicians) {
            layoutTechnicians.addView(createTechnicianCard(technician));
        }

        if (realTechnicianCount <= 1) {
            carouselPosition = 0;
            technicianCarouselReady = true;
            return;
        }

        int middleRepeat = repeatCount / 2;
        carouselPosition = middleRepeat * realTechnicianCount;

        layoutTechnicians.post(() -> {
            adjustingTechnicianCarousel = true;
            scrollTechnicians.scrollTo(getCenteredScrollXForPosition(carouselPosition), 0);
            adjustingTechnicianCarousel = false;
            technicianCarouselReady = true;
        });
    }

    private void setupTechnicianCarouselScroll() {
        scrollTechnicians.setOnScrollChangeListener((view, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (!technicianCarouselReady || adjustingTechnicianCarousel || realTechnicianCount <= 1) {
                return;
            }

            technicianCarouselHandler.removeCallbacks(snapTechnicianRunnable);
            technicianCarouselHandler.postDelayed(snapTechnicianRunnable, 170);
        });
    }

    private void snapTechnicianCarousel() {
        if (!technicianCarouselReady || carouselTechnicians.isEmpty() || realTechnicianCount <= 1) {
            return;
        }

        int nearestPosition = findNearestTechnicianPosition();
        int normalizedPosition = normalizeTechnicianCarouselPosition(nearestPosition);

        carouselPosition = normalizedPosition;

        int targetScrollX = getCenteredScrollXForPosition(normalizedPosition);

        if (normalizedPosition != nearestPosition) {
            adjustingTechnicianCarousel = true;
            scrollTechnicians.scrollTo(targetScrollX, 0);
            adjustingTechnicianCarousel = false;
        } else {
            scrollTechnicians.smoothScrollTo(targetScrollX, 0);
        }
    }

    private int findNearestTechnicianPosition() {
        if (layoutTechnicians.getChildCount() == 0) {
            return 0;
        }

        int viewportCenterX = scrollTechnicians.getScrollX() + (scrollTechnicians.getWidth() / 2);
        int nearestPosition = 0;
        int nearestDistance = Integer.MAX_VALUE;

        for (int i = 0; i < layoutTechnicians.getChildCount(); i++) {
            View child = layoutTechnicians.getChildAt(i);
            int childCenterX = child.getLeft() + (child.getWidth() / 2);
            int distance = Math.abs(viewportCenterX - childCenterX);

            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearestPosition = i;
            }
        }

        return nearestPosition;
    }

    private int normalizeTechnicianCarouselPosition(int position) {
        if (realTechnicianCount <= 1 || carouselTechnicians.isEmpty()) {
            return position;
        }

        int safeZone = realTechnicianCount * 4;
        int lastIndex = carouselTechnicians.size() - 1;

        if (position > safeZone && position < lastIndex - safeZone) {
            return position;
        }

        int modulo = position % realTechnicianCount;
        int middleBase = (carouselTechnicians.size() / 2 / realTechnicianCount) * realTechnicianCount;

        return middleBase + modulo;
    }

    private int getCenteredScrollXForPosition(int position) {
        if (layoutTechnicians.getChildCount() == 0) {
            return 0;
        }

        int safePosition = Math.max(0, Math.min(position, layoutTechnicians.getChildCount() - 1));
        View child = layoutTechnicians.getChildAt(safePosition);

        int centeredX = child.getLeft() - ((scrollTechnicians.getWidth() - child.getWidth()) / 2);

        return Math.max(0, centeredX);
    }

    private LinearLayout createTechnicianCard(CustomerTechnicianResponse technician) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14), dp(14), dp(14), dp(14));
        card.setBackground(makeStrokeRounded("#FFFFFF", "#DCE6EB", 18, 1));
        card.setElevation(dp(2));

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(dp(240), ViewGroup.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(0, 0, dp(14), dp(6));
        card.setLayoutParams(cardParams);

        // Top row
        LinearLayout topRow = new LinearLayout(this);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView avatar = new TextView(this);
        LinearLayout.LayoutParams avatarParams = new LinearLayout.LayoutParams(dp(52), dp(52));
        avatar.setLayoutParams(avatarParams);
        avatar.setGravity(Gravity.CENTER);
        avatar.setBackground(makeOval("#EAF4FF"));
        avatar.setText(getInitial(technician == null ? null : technician.name));
        avatar.setTextColor(Color.parseColor("#2F4A8A"));
        avatar.setTextSize(22);
        avatar.setTypeface(Typeface.DEFAULT_BOLD);

        LinearLayout infoWrap = new LinearLayout(this);
        LinearLayout.LayoutParams infoWrapParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        infoWrapParams.setMargins(dp(12), 0, dp(8), 0);
        infoWrap.setLayoutParams(infoWrapParams);
        infoWrap.setOrientation(LinearLayout.VERTICAL);

        TextView name = new TextView(this);
        name.setText(technician == null || technician.name == null ? "Teknisi" : technician.name);
        name.setTextColor(Color.parseColor("#1F2329"));
        name.setTextSize(17);
        name.setTypeface(Typeface.DEFAULT_BOLD);
        name.setMaxLines(1);
        name.setEllipsize(TextUtils.TruncateAt.END);

        TextView skill = new TextView(this);
        skill.setText(getTechnicianSubtext(technician));
        skill.setTextColor(Color.parseColor("#6B7680"));
        skill.setTextSize(13);
        skill.setMaxLines(2);
        skill.setEllipsize(TextUtils.TruncateAt.END);

        infoWrap.addView(name);
        infoWrap.addView(skill);

        TextView ratingChip = new TextView(this);
        ratingChip.setPadding(dp(10), dp(5), dp(10), dp(5));
        ratingChip.setBackground(makeStrokeRounded("#F5FAFF", "#D6E4F0", 12, 1));
        ratingChip.setText("★ " + formatRating(technician == null ? null : technician.averageRating));
        ratingChip.setTextColor(Color.parseColor("#2F4A8A"));
        ratingChip.setTextSize(12);
        ratingChip.setTypeface(Typeface.DEFAULT_BOLD);

        topRow.addView(avatar);
        topRow.addView(infoWrap);
        topRow.addView(ratingChip);

        // Divider spacing
        TextView spacer = new TextView(this);
        spacer.setHeight(dp(10));

        // Stats row
        LinearLayout statsRow = new LinearLayout(this);
        statsRow.setOrientation(LinearLayout.HORIZONTAL);
        statsRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView jobsChip = new TextView(this);
        jobsChip.setPadding(dp(10), dp(5), dp(10), dp(5));
        jobsChip.setBackground(makeStrokeRounded("#F8FBFC", "#E1EAEE", 12, 1));
        jobsChip.setText(formatJobs(technician == null ? null : technician.totalJobs));
        jobsChip.setTextColor(Color.parseColor("#5F6B73"));
        jobsChip.setTextSize(12);

        TextView statusChip = new TextView(this);
        LinearLayout.LayoutParams statusParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        statusParams.setMargins(dp(8), 0, 0, 0);
        statusChip.setLayoutParams(statusParams);
        statusChip.setPadding(dp(10), dp(5), dp(10), dp(5));
        statusChip.setBackground(makeStrokeRounded("#EDF9F0", "#CFE9D6", 12, 1));
        statusChip.setText("Tersedia");
        statusChip.setTextColor(Color.parseColor("#2E7D32"));
        statusChip.setTextSize(12);

        statsRow.addView(jobsChip);
        statsRow.addView(statusChip);

        // Button row
        TextView button = new TextView(this);
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(38)
        );
        buttonParams.setMargins(0, dp(12), 0, 0);
        button.setLayoutParams(buttonParams);
        button.setGravity(Gravity.CENTER);
        button.setBackground(makeRounded("#445D9B", 12));
        button.setText("More Info");
        button.setTextColor(Color.WHITE);
        button.setTextSize(13);
        button.setTypeface(Typeface.DEFAULT_BOLD);

        card.addView(topRow);
        card.addView(spacer);
        card.addView(statsRow);
        card.addView(button);

button.setOnClickListener(v -> openTechnicianInfo(technician));

        card.setOnClickListener(v -> openTechnicianInfo(technician));

        return card;
    }

    private void showCategoryMessage(String message) {
        gridCategories.setVisibility(android.view.View.GONE);
        txtCategoryEmpty.setVisibility(android.view.View.VISIBLE);
        txtCategoryEmpty.setText(message);
    }

    private void showTechnicianMessage(String message) {
        layoutTechnicians.removeAllViews();
        scrollTechnicians.setVisibility(android.view.View.GONE);
        txtTechnicianEmpty.setVisibility(android.view.View.VISIBLE);
        txtTechnicianEmpty.setText(message);
    }

    private String getCategoryShortName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "DV";
        }

        String lower = name.toLowerCase();

        if (lower.contains("air") || lower.contains("ac")) return "AC";
        if (lower.contains("tv") || lower.contains("television")) return "TV";
        if (lower.contains("fan")) return "FN";
        if (lower.contains("rice")) return "RC";
        if (lower.contains("wash")) return "WM";
        if (lower.contains("fridge") || lower.contains("kulkas")) return "FR";
        if (lower.contains("oven")) return "OV";

        String[] parts = name.trim().split("\\s+");

        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }

        String first = parts[0].substring(0, 1);
        String second = parts[1].substring(0, 1);
        return (first + second).toUpperCase();
    }

    private String getTechnicianSubtext(CustomerTechnicianResponse technician) {
        if (technician == null) {
            return "Teknisi terdaftar";
        }

        if (technician.supportedDeviceCategories != null && !technician.supportedDeviceCategories.isEmpty()) {
            StringBuilder builder = new StringBuilder();

            int limit = Math.min(technician.supportedDeviceCategories.size(), 2);

            for (int i = 0; i < limit; i++) {
                DeviceCategoryResponse category = technician.supportedDeviceCategories.get(i);

                if (category != null && category.name != null) {
                    if (builder.length() > 0) {
                        builder.append(" • ");
                    }

                    builder.append(category.name);
                }
            }

            if (builder.length() > 0) {
                return builder.toString();
            }
        }

        if (technician.description != null && !technician.description.trim().isEmpty()) {
            return technician.description.trim();
        }

        return "Teknisi terdaftar";
    }

    private String formatRating(BigDecimal rating) {
        if (rating == null) {
            return "0.0";
        }

        return rating.setScale(1, java.math.RoundingMode.HALF_UP).toPlainString();
    }

    private String formatJobs(Integer totalJobs) {
        int jobs = totalJobs == null ? 0 : totalJobs;
        return jobs + " pekerjaan";
    }

    private void openTechnicianInfo(CustomerTechnicianResponse technician) {
        if (technician == null || isBlank(technician.technicianProfileId)) {
            Toast.makeText(this, "Data teknisi tidak valid.", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(CustomerHomeActivity.this, TechnicianDetailActivity.class);
        intent.putExtra(TechnicianDetailActivity.EXTRA_TECHNICIAN_ID, technician.technicianProfileId);
        intent.putExtra(TechnicianDetailActivity.EXTRA_TECHNICIAN_NAME, technician.name);

        startActivity(intent);
    }

    private String getDisplayName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "Pelanggan";
        }

        return name.trim();
    }

    private String getInitial(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "U";
        }

        return name.trim().substring(0, 1).toUpperCase();
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

    private GradientDrawable makeOval(String color) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setColor(Color.parseColor(color));
        return drawable;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        technicianCarouselHandler.removeCallbacks(snapTechnicianRunnable);
    }

    private void logout() {
        tokenManager.clearSession();
        ApiClient.reset();

        Intent intent = new Intent(CustomerHomeActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}

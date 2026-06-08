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
import com.teknisio.mobile.view.customer.helper.CustomerCategoryRenderer;
import com.teknisio.mobile.view.customer.helper.CustomerTechnicianCardRenderer;

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
    private TextView txtSeeAllTechnicians;
    private TextView txtSeeAllNews;
    private LinearLayout cardNews;
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
        setupHomeActions();
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
        txtSeeAllTechnicians = findViewById(R.id.txtSeeAllTechnicians);
        txtSeeAllNews = findViewById(R.id.txtSeeAllNews);
        cardNews = findViewById(R.id.cardNews);
        btnNotification = findViewById(R.id.btnNotification);

        navChat = findViewById(R.id.navChat);
        navHistory = findViewById(R.id.navHistory);
        navAccount = findViewById(R.id.navAccount);
    }


    private void setupHomeActions() {
        if (txtSeeAllTechnicians != null) {
            txtSeeAllTechnicians.setOnClickListener(v -> openAllTechnicians());
        }

        if (txtSeeAllNews != null) {
            txtSeeAllNews.setOnClickListener(v -> openNewsList());
        }

        if (cardNews != null) {
            cardNews.setOnClickListener(v -> openFeaturedNews());
            cardNews.setClickable(true);
        }
    }

    private void openAllTechnicians() {
        Intent intent = new Intent(CustomerHomeActivity.this, TechnicianListActivity.class);
        intent.putExtra(TechnicianListActivity.EXTRA_SHOW_ALL, true);
        startActivity(intent);
    }

    private void openNewsList() {
        Intent intent = new Intent(CustomerHomeActivity.this, NewsActivity.class);
        startActivity(intent);
    }

    private void openFeaturedNews() {
        Intent intent = new Intent(CustomerHomeActivity.this, NewsActivity.class);
        intent.putExtra(NewsActivity.EXTRA_NEWS_ID, NewsActivity.NEWS_ID_FEATURED);
        startActivity(intent);
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

        navChat.setOnClickListener(v -> {
            Intent intent = new Intent(CustomerHomeActivity.this, OrderHistoryActivity.class);
            startActivity(intent);
        });

        navHistory.setOnClickListener(v -> {
            Intent intent = new Intent(CustomerHomeActivity.this, OrderHistoryActivity.class);
            startActivity(intent);
        });

        navAccount.setOnClickListener(v -> {
            Intent intent = new Intent(CustomerHomeActivity.this, AccountActivity.class);
            startActivity(intent);
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
                        showCategoryMessage("Tidak bisa terhubung ke server: " + t.getMessage());
                        showTechnicianMessage("Teknisi belum bisa dimuat.");
                    }
                });
    }

    private void loadAllTechnicians() {
        allTechnicians.clear();
        carouselTechnicians.clear();
        pendingTechnicianRequests = 0;
        realTechnicianCount = 0;
        carouselPosition = 0;
        technicianCarouselReady = false;
        technicianCarouselHandler.removeCallbacks(snapTechnicianRunnable);

        if (categories.isEmpty()) {
            showTechnicianMessage("Belum ada kategori untuk memuat teknisi.");
            return;
        }

        DeviceCategoryResponse featuredCategory = null;

        for (DeviceCategoryResponse category : categories) {
            if (category != null
                    && category.deviceCategoryId != null
                    && !category.deviceCategoryId.trim().isEmpty()) {
                featuredCategory = category;
                break;
            }
        }

        if (featuredCategory == null) {
            showTechnicianMessage("Belum ada kategori valid untuk memuat teknisi.");
            return;
        }

        showTechnicianMessage("Memuat teknisi rekomendasi...");

        ApiClient.getApiService(this)
                .searchTechnicians(featuredCategory.deviceCategoryId, "ONLINE", "rating")
                .enqueue(new Callback<ApiResponse<List<CustomerTechnicianResponse>>>() {
                    @Override
                    public void onResponse(
                            Call<ApiResponse<List<CustomerTechnicianResponse>>> call,
                            Response<ApiResponse<List<CustomerTechnicianResponse>>> response
                    ) {
                        if (!response.isSuccessful()
                                || response.body() == null
                                || !response.body().success
                                || response.body().data == null) {
                            showTechnicianMessage("Teknisi rekomendasi belum bisa dimuat.");
                            return;
                        }

                        allTechnicians.clear();

                        for (CustomerTechnicianResponse technician : response.body().data) {
                            addUniqueTechnician(technician);

                            if (allTechnicians.size() >= 8) {
                                break;
                            }
                        }

                        if (allTechnicians.isEmpty()) {
                            showTechnicianMessage("Belum ada teknisi online untuk rekomendasi.");
                            return;
                        }

                        renderTechnicians(allTechnicians);
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<CustomerTechnicianResponse>>> call, Throwable t) {
                        showTechnicianMessage("Tidak bisa terhubung ke server: " + t.getMessage());
                    }
                });
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
        CustomerCategoryRenderer.render(
                this,
                gridCategories,
                txtCategoryEmpty,
                categories,
                selectedCategoryId,
                category -> {
                    selectedCategoryId = category.deviceCategoryId;
                    renderCategories();
                    openOrderByCategory(category);
                }
        );
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
                CustomerCategoryRenderer.getCategoryDisplayName(category.name).replace("\n", " ")
        );
        startActivity(intent);
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
            layoutTechnicians.addView(CustomerTechnicianCardRenderer.create(this, technician, this::openTechnicianInfo));
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

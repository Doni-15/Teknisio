package com.teknisio.mobile.view.customer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.teknisio.mobile.R;
import com.teknisio.mobile.local.TokenManager;
import com.teknisio.mobile.model.response.ApiResponse;
import com.teknisio.mobile.model.response.AuthUserResponse;
import com.teknisio.mobile.network.ApiClient;
import com.teknisio.mobile.util.BackButtonHelper;
import com.teknisio.mobile.view.auth.LoginActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AccountActivity extends AppCompatActivity {

    private FrameLayout btnBack;
    private FrameLayout layoutProfileAvatar;
    private ImageView imgProfileAvatar;
    private TextView txtProfileAvatarInitial;
    private TextView txtProfileNameLarge;
    private TextView txtVerified;
    private TextView txtProfileName;
    private TextView txtProfileEmail;
    private TextView txtProfilePhone;

    private ImageView btnEditName;
    private ImageView btnEditPhone;

    private LinearLayout rowLanguage;
    private LinearLayout rowTerms;
    private LinearLayout rowPrivacy;
    private LinearLayout rowCallCenter;

    private androidx.appcompat.widget.AppCompatButton btnLogout;

    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        tokenManager = new TokenManager(this);

        bindViews();
        renderFromSession();
        setupActions();
        refreshProfile();
    }

    private void bindViews() {
        btnBack = findViewById(R.id.btnBack);
        layoutProfileAvatar = findViewById(R.id.layoutProfileAvatar);
        imgProfileAvatar = findViewById(R.id.imgProfileAvatar);
        txtProfileAvatarInitial = findViewById(R.id.txtProfileAvatarInitial);
        txtProfileNameLarge = findViewById(R.id.txtProfileNameLarge);
        txtVerified = findViewById(R.id.txtVerified);
        txtProfileName = findViewById(R.id.txtProfileName);
        txtProfileEmail = findViewById(R.id.txtProfileEmail);
        txtProfilePhone = findViewById(R.id.txtProfilePhone);

        btnEditName = findViewById(R.id.btnEditName);
        btnEditPhone = findViewById(R.id.btnEditPhone);

        rowLanguage = findViewById(R.id.rowLanguage);
        rowTerms = findViewById(R.id.rowTerms);
        rowPrivacy = findViewById(R.id.rowPrivacy);
        rowCallCenter = findViewById(R.id.rowCallCenter);

        btnLogout = findViewById(R.id.btnLogout);
    }

    private void renderFromSession() {
        renderProfile(
                tokenManager.getName(),
                tokenManager.getEmail(),
                tokenManager.getPhoneNumber(),
                tokenManager.getAccountStatus(),
                tokenManager.getProfilePhoto()
        );
    }

    private void renderProfile(String name, String email, String phoneNumber, String accountStatus, String profilePhoto) {
        String safeName = isBlank(name) ? "Customer" : name.trim();
        String safeEmail = isBlank(email) ? "-" : email.trim();
        String safePhone = isBlank(phoneNumber) ? "-" : phoneNumber.trim();

        loadProfileAvatar(profilePhoto, safeName);
        txtProfileNameLarge.setText(safeName);
        txtProfileName.setText(safeName);
        txtProfileEmail.setText(safeEmail);
        txtProfilePhone.setText(safePhone);

        if ("ACTIVE".equalsIgnoreCase(accountStatus)) {
            txtVerified.setText("Verified");
        } else if (!isBlank(accountStatus)) {
            txtVerified.setText(formatStatus(accountStatus));
        } else {
            txtVerified.setText("Verified");
        }
    }

    private void loadProfileAvatar(String profilePhoto, String name) {
        txtProfileAvatarInitial.setText(getInitial(name));

        /*
         * Tempat avatar asli nanti.
         *
         * Kalau nanti sudah pakai image loader seperti Glide, cukup ganti isi method ini:
         *
         * Glide.with(this)
         *      .load(profilePhoto)
         *      .circleCrop()
         *      .into(imgProfileAvatar);
         *
         * Lalu set:
         * imgProfileAvatar.setVisibility(android.view.View.VISIBLE);
         * txtProfileAvatarInitial.setVisibility(android.view.View.GONE);
         */

        imgProfileAvatar.setVisibility(android.view.View.GONE);
        txtProfileAvatarInitial.setVisibility(android.view.View.VISIBLE);
    }

    private void setupActions() {
        BackButtonHelper.setup(btnBack, this::finish);

        btnEditName.setOnClickListener(v ->
                Toast.makeText(this, "Edit nama belum tersedia.", Toast.LENGTH_SHORT).show()
        );

        btnEditPhone.setOnClickListener(v ->
                Toast.makeText(this, "Edit nomor telepon belum tersedia.", Toast.LENGTH_SHORT).show()
        );

        rowLanguage.setOnClickListener(v ->
                Toast.makeText(this, "Pengaturan bahasa belum tersedia.", Toast.LENGTH_SHORT).show()
        );

        rowTerms.setOnClickListener(v ->
                Toast.makeText(this, "Terms & Condition belum tersedia.", Toast.LENGTH_SHORT).show()
        );

        rowPrivacy.setOnClickListener(v ->
                Toast.makeText(this, "Privacy Policy belum tersedia.", Toast.LENGTH_SHORT).show()
        );

        rowCallCenter.setOnClickListener(v ->
                Toast.makeText(this, "Call Center belum tersedia.", Toast.LENGTH_SHORT).show()
        );

        btnLogout.setOnClickListener(v -> logout());
    }

    private void refreshProfile() {
        ApiClient.getApiService(this)
                .getProfile()
                .enqueue(new Callback<ApiResponse<AuthUserResponse>>() {
                    @Override
                    public void onResponse(
                            Call<ApiResponse<AuthUserResponse>> call,
                            Response<ApiResponse<AuthUserResponse>> response
                    ) {
                        if (response.code() == 401 || response.code() == 403) {
                            Toast.makeText(AccountActivity.this, "Session berakhir. Silakan login kembali.", Toast.LENGTH_SHORT).show();
                            logout();
                            return;
                        }

                        if (!response.isSuccessful()
                                || response.body() == null
                                || !response.body().success
                                || response.body().data == null) {
                            return;
                        }

                        AuthUserResponse profile = response.body().data;

                        tokenManager.saveUser(profile);

                        renderProfile(
                                profile.name,
                                profile.email,
                                profile.phoneNumber,
                                profile.accountStatus,
                                profile.profilePhoto
                        );
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<AuthUserResponse>> call, Throwable t) {
                        // Tetap pakai data session lokal.
                    }
                });
    }

    private void logout() {
        tokenManager.clearSession();
        ApiClient.reset();

        Intent intent = new Intent(AccountActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private String getInitial(String name) {
        if (isBlank(name)) {
            return "U";
        }

        return name.trim().substring(0, 1).toUpperCase();
    }

    private String formatStatus(String status) {
        String normalized = status.trim().replace("_", " ").toLowerCase();

        return normalized.substring(0, 1).toUpperCase() + normalized.substring(1);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}

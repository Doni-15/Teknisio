package com.teknisio.mobile.view.customer;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatButton;

import com.teknisio.mobile.R;
import com.teknisio.mobile.base.BaseActivity;
import com.teknisio.mobile.local.TokenManager;
import com.teknisio.mobile.model.response.ApiResponse;
import com.teknisio.mobile.model.response.AuthUserResponse;
import com.teknisio.mobile.network.ApiClient;
import com.teknisio.mobile.util.AppToast;
import com.teknisio.mobile.util.BackButtonHelper;
import com.teknisio.mobile.util.ErrorParser;
import com.teknisio.mobile.view.auth.LoginActivity;
import com.teknisio.mobile.view.technician.TechnicianSkillActivity;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AccountActivity extends BaseActivity {

    private static final String EXTRA_ROW_TAG = "extra_account_row";

    private FrameLayout btnBack;
    private FrameLayout layoutProfileAvatar;
    private ImageView imgProfileAvatar;
    private TextView txtProfileAvatarInitial;
    private TextView txtProfileNameLarge;
    private TextView txtVerified;
    private TextView txtProfileName;
    private TextView txtProfileEmail;
    private TextView txtProfilePhone;

    private LinearLayout cardAccountInfo;

    private ImageView btnEditName;
    private ImageView btnEditPhone;

    private LinearLayout rowLanguage;
    private LinearLayout rowTerms;
    private LinearLayout rowPrivacy;
    private LinearLayout rowCallCenter;

    private AppCompatButton btnLogout;

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
        cardAccountInfo = findViewById(R.id.cardAccountInfo);

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
                tokenManager.getProfilePhoto(),
                tokenManager.getRole(),
                tokenManager.getAddress(),
                tokenManager.getTechnicianProfileId()
        );
    }

    private void renderProfile(
            String name,
            String email,
            String phoneNumber,
            String accountStatus,
            String profilePhoto,
            String role,
            String address,
            String technicianProfileId
    ) {
        String safeRole = isBlank(role) ? "CUSTOMER" : role.trim();
        String safeName = formatDisplayName(
                isBlank(name)
                        ? (isTechnicianRole(safeRole) ? "Teknisi" : "Pelanggan")
                        : name.trim()
        );
        String safeEmail = isBlank(email) ? "-" : email.trim();
        String safePhone = isBlank(phoneNumber) ? "-" : phoneNumber.trim();

        loadProfileAvatar(profilePhoto, safeName);

        txtProfileNameLarge.setText(safeName);
        txtProfileName.setText(safeName);
        txtProfileEmail.setText(safeEmail);
        txtProfilePhone.setText(safePhone);
        txtVerified.setText(buildStatusText(accountStatus, safeRole));

        renderExtraAccountRows(safeRole, address, technicianProfileId);
    }

    private void loadProfileAvatar(String profilePhoto, String name) {
        txtProfileAvatarInitial.setText(getInitial(name));

        /*
         * Avatar asli nanti bisa diisi dengan image loader seperti Glide.
         * Untuk sekarang tetap pakai initial supaya stabil dan tidak menambah dependency.
         */

        imgProfileAvatar.setVisibility(View.GONE);
        txtProfileAvatarInitial.setVisibility(View.VISIBLE);
    }

    private void renderExtraAccountRows(String role, String address, String technicianProfileId) {
        if (cardAccountInfo == null) {
            return;
        }

        removeExtraAccountRows();


        cardAccountInfo.addView(createDivider());
        cardAccountInfo.addView(createEditableMultilineRow(
                "Alamat",
                isBlank(address) ? "-" : address.trim(),
                () -> showEditFieldDialog(
                        "Ubah Alamat",
                        "Alamat lengkap",
                        tokenManager.getAddress(),
                        "address",
                        true
                )
        ));

        if (isTechnicianRole(role)) {

            cardAccountInfo.addView(createDivider());
            cardAccountInfo.addView(createActionRow(
                    "Keahlian",
                    "Atur kategori perangkat",
                    this::openTechnicianSkills
            ));
        }
    }

    private void removeExtraAccountRows() {
        for (int i = cardAccountInfo.getChildCount() - 1; i >= 0; i--) {
            View child = cardAccountInfo.getChildAt(i);

            if (EXTRA_ROW_TAG.equals(child.getTag())) {
                cardAccountInfo.removeViewAt(i);
            }
        }
    }

    private View createDivider() {
        View divider = new View(this);
        divider.setTag(EXTRA_ROW_TAG);
        divider.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                1
        ));
        divider.setBackgroundColor(Color.parseColor("#D7DDE1"));
        return divider;
    }

    private LinearLayout createInfoRow(String label, String value) {
        LinearLayout row = new LinearLayout(this);
        row.setTag(EXTRA_ROW_TAG);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);
        row.setPadding(dp(28), 0, dp(20), 0);
        row.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(76)
        ));

        LinearLayout textContainer = new LinearLayout(this);
        textContainer.setOrientation(LinearLayout.VERTICAL);
        textContainer.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
        ));

        TextView txtLabel = new TextView(this);
        txtLabel.setText(label);
        txtLabel.setTextColor(Color.parseColor("#6B7680"));
        txtLabel.setTextSize(13);

        TextView txtValue = new TextView(this);
        txtValue.setText(isBlank(value) ? "-" : value.trim());
        txtValue.setTextColor(Color.parseColor("#1F2329"));
        txtValue.setTextSize(15);
        txtValue.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        txtValue.setSingleLine(true);
        txtValue.setEllipsize(android.text.TextUtils.TruncateAt.END);

        LinearLayout.LayoutParams valueParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        valueParams.setMargins(0, dp(3), 0, 0);
        txtValue.setLayoutParams(valueParams);

        textContainer.addView(txtLabel);
        textContainer.addView(txtValue);
        row.addView(textContainer);

        return row;
    }

    private LinearLayout createMultilineInfoRow(String label, String value) {
        LinearLayout row = new LinearLayout(this);
        row.setTag(EXTRA_ROW_TAG);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);
        row.setPadding(dp(28), dp(16), dp(20), dp(16));
        row.setMinimumHeight(dp(92));
        row.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        TextView txtLabel = new TextView(this);
        txtLabel.setText(label);
        txtLabel.setTextColor(Color.parseColor("#6B7680"));
        txtLabel.setTextSize(13);

        TextView txtValue = new TextView(this);
        txtValue.setText(isBlank(value) ? "-" : value.trim());
        txtValue.setTextColor(Color.parseColor("#1F2329"));
        txtValue.setTextSize(15);
        txtValue.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        txtValue.setSingleLine(false);
        txtValue.setLineSpacing(dp(3), 1.0f);

        LinearLayout.LayoutParams valueParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        valueParams.setMargins(0, dp(5), 0, 0);
        txtValue.setLayoutParams(valueParams);

        row.addView(txtLabel);
        row.addView(txtValue);

        return row;
    }

    private LinearLayout createActionRow(String label, String value, Runnable action) {
        LinearLayout row = createInfoRow(label, value);

        TypedArray typedArray = getTheme().obtainStyledAttributes(
                new int[]{android.R.attr.selectableItemBackground}
        );

        try {
            row.setForeground(typedArray.getDrawable(0));
        } finally {
            typedArray.recycle();
        }

        row.setOnClickListener(v -> {
            if (action != null) {
                action.run();
            }
        });

        return row;
    }

    private LinearLayout createEditableMultilineRow(String label, String value, Runnable onEdit) {
        LinearLayout row = new LinearLayout(this);
        row.setTag(EXTRA_ROW_TAG);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(android.view.Gravity.TOP);
        row.setPadding(dp(28), dp(16), dp(20), dp(16));
        row.setMinimumHeight(dp(92));
        row.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        LinearLayout textCol = new LinearLayout(this);
        textCol.setOrientation(LinearLayout.VERTICAL);
        textCol.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView txtLabel = new TextView(this);
        txtLabel.setText(label);
        txtLabel.setTextColor(Color.parseColor("#6B7680"));
        txtLabel.setTextSize(13);

        TextView txtValue = new TextView(this);
        txtValue.setText(isBlank(value) ? "-" : value.trim());
        txtValue.setTextColor(Color.parseColor("#1F2329"));
        txtValue.setTextSize(15);
        txtValue.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        txtValue.setSingleLine(false);
        txtValue.setLineSpacing(dp(3), 1.0f);

        LinearLayout.LayoutParams valueParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        valueParams.setMargins(0, dp(5), 0, 0);
        txtValue.setLayoutParams(valueParams);

        textCol.addView(txtLabel);
        textCol.addView(txtValue);

        android.widget.ImageView editIcon = new android.widget.ImageView(this);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(24), dp(24));
        iconParams.setMargins(dp(8), dp(4), 0, 0);
        editIcon.setLayoutParams(iconParams);
        editIcon.setImageResource(R.drawable.ic_edit);
        editIcon.setOnClickListener(v -> { if (onEdit != null) onEdit.run(); });

        row.addView(textCol);
        row.addView(editIcon);
        return row;
    }

    private void showEditFieldDialog(String title, String hint, String currentValue, String fieldKey, boolean multiline) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);

        final EditText input = new EditText(this);
        input.setHint(hint);
        input.setText(isBlank(currentValue) ? "" : currentValue.trim());
        if (multiline) {
            input.setSingleLine(false);
            input.setMaxLines(4);
            input.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        } else {
            input.setSingleLine(true);
            if ("phoneNumber".equals(fieldKey)) {
                input.setInputType(android.text.InputType.TYPE_CLASS_PHONE);
            } else {
                input.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_FLAG_CAP_WORDS);
            }
        }
        int pad = dp(16);
        input.setPadding(pad, pad, pad, pad);

        builder.setView(input);
        builder.setNegativeButton("Batal", (d, w) -> d.dismiss());
        builder.setPositiveButton("Simpan", (d, w) -> {
            String newValue = input.getText() == null ? "" : input.getText().toString().trim();
            if (newValue.isEmpty()) {
                AppToast.error(this, hint + " tidak boleh kosong.");
                return;
            }
            submitProfileUpdate(fieldKey, newValue);
        });
        builder.show();
    }

    private void submitProfileUpdate(String fieldKey, String newValue) {
        Map<String, String> body = new HashMap<>();
        body.put(fieldKey, newValue);

        ApiClient.getApiService(this)
                .updateProfile(body)
                .enqueue(new Callback<ApiResponse<AuthUserResponse>>() {
                    @Override
                    public void onResponse(
                            Call<ApiResponse<AuthUserResponse>> call,
                            Response<ApiResponse<AuthUserResponse>> response
                    ) {
                        if (!response.isSuccessful()) {
                            AppToast.error(AccountActivity.this,
                                    ErrorParser.parseError(response, "Gagal memperbarui profil."));
                            return;
                        }
                        ApiResponse<AuthUserResponse> body = response.body();
                        if (body == null || !body.success || body.data == null) {
                            AppToast.error(AccountActivity.this,
                                    ErrorParser.getBestMessage(body, "Gagal memperbarui profil."));
                            return;
                        }
                        tokenManager.saveUser(body.data);
                        renderProfile(
                                body.data.name,
                                body.data.email,
                                body.data.phoneNumber,
                                body.data.accountStatus,
                                body.data.profilePhoto,
                                body.data.role,
                                body.data.address,
                                body.data.technicianProfileId
                        );
                        AppToast.success(AccountActivity.this, "Profil berhasil diperbarui.");
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<AuthUserResponse>> call, Throwable t) {
                        AppToast.error(AccountActivity.this, "Tidak bisa terhubung ke server.");
                    }
                });
    }

    private void openTechnicianSkills() {
        Intent intent = new Intent(AccountActivity.this, TechnicianSkillActivity.class);
        startActivity(intent);
    }

    private void setupActions() {
        BackButtonHelper.setup(btnBack, this::finish);

        if (btnEditName != null) {
            btnEditName.setOnClickListener(v -> showEditFieldDialog(
                    "Ubah Nama",
                    "Nama",
                    tokenManager.getName(),
                    "name",
                    false
            ));
        }

        if (btnEditPhone != null) {
            btnEditPhone.setOnClickListener(v -> showEditFieldDialog(
                    "Ubah Nomor Telepon",
                    "Nomor Telepon (contoh: 08123456789)",
                    tokenManager.getPhoneNumber(),
                    "phoneNumber",
                    false
            ));
        }

        if (rowLanguage != null) {
            rowLanguage.setOnClickListener(v ->
                    Toast.makeText(this, "Pengaturan bahasa belum tersedia.", Toast.LENGTH_SHORT).show()
            );
        }

        if (rowTerms != null) {
            rowTerms.setOnClickListener(v ->
                    Toast.makeText(this, "Syarat dan ketentuan belum tersedia.", Toast.LENGTH_SHORT).show()
            );
        }

        if (rowPrivacy != null) {
            rowPrivacy.setOnClickListener(v ->
                    Toast.makeText(this, "Kebijakan privasi belum tersedia.", Toast.LENGTH_SHORT).show()
            );
        }

        if (rowCallCenter != null) {
            rowCallCenter.setOnClickListener(v ->
                    Toast.makeText(this, "Pusat bantuan belum tersedia.", Toast.LENGTH_SHORT).show()
            );
        }

        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> logout());
        }
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
                            Toast.makeText(
                                    AccountActivity.this,
                                    "Session berakhir. Silakan login kembali.",
                                    Toast.LENGTH_SHORT
                            ).show();
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
                                profile.profilePhoto,
                                profile.role,
                                profile.address,
                                profile.technicianProfileId
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

    private String buildStatusText(String status, String role) {
        String accountStatus = isBlank(status) ? "ACTIVE" : status.trim();

        return formatRole(role) + " • " + formatStatus(accountStatus);
    }

    private String formatDisplayName(String value) {
        if (isBlank(value)) {
            return "";
        }

        String[] words = value.trim().replaceAll("\\s+", " ").split(" ");
        StringBuilder builder = new StringBuilder();

        for (String word : words) {
            if (word.isEmpty()) {
                continue;
            }

            if (builder.length() > 0) {
                builder.append(" ");
            }

            if (word.length() == 1) {
                builder.append(word.toUpperCase());
            } else {
                builder.append(word.substring(0, 1).toUpperCase());
                builder.append(word.substring(1).toLowerCase());
            }
        }

        return builder.toString();
    }

    private String getInitial(String name) {
        if (isBlank(name)) {
            return "U";
        }

        String[] parts = name.trim().split("\\s+");

        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }

        return (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase();
    }

    private String formatStatus(String status) {
        if (isBlank(status)) {
            return "Aktif";
        }

        String normalized = status.trim().replace("_", " ").toUpperCase();

        switch (normalized) {
            case "ACTIVE":
                return "Aktif";
            case "INACTIVE":
                return "Tidak Aktif";
            case "PENDING":
                return "Menunggu Verifikasi";
            case "SUSPENDED":
                return "Ditangguhkan";
            case "REJECTED":
                return "Ditolak";
            default:
                return formatDisplayName(status.replace("_", " "));
        }
    }

    private String formatRole(String role) {
        if (isTechnicianRole(role)) {
            return "Teknisi";
        }

        if ("CUSTOMER".equalsIgnoreCase(role)) {
            return "Pelanggan";
        }

        return isBlank(role) ? "Pengguna" : formatDisplayName(role.replace("_", " "));
    }

    private boolean isTechnicianRole(String role) {
        return "TECHNICIAN".equalsIgnoreCase(role);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}

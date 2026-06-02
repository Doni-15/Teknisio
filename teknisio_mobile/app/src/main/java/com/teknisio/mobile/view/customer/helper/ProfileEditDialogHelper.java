package com.teknisio.mobile.view.customer.helper;

import android.app.AlertDialog;
import android.text.InputType;
import android.widget.EditText;

import com.teknisio.mobile.base.BaseActivity;
import com.teknisio.mobile.model.response.ApiResponse;
import com.teknisio.mobile.model.response.AuthUserResponse;
import com.teknisio.mobile.network.ApiClient;
import com.teknisio.mobile.util.AppToast;
import com.teknisio.mobile.util.ErrorParser;
import com.teknisio.mobile.util.TextHelper;
import com.teknisio.mobile.util.ViewHelper;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public final class ProfileEditDialogHelper {

    public interface ProfileUpdateCallback {
        void onProfileUpdated(AuthUserResponse profile);
    }

    private ProfileEditDialogHelper() {
    }

    public static void show(
            BaseActivity activity,
            String title,
            String hint,
            String currentValue,
            String fieldKey,
            boolean multiline,
            ProfileUpdateCallback callback
    ) {
        if (activity == null || TextHelper.isBlank(fieldKey)) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(title);

        final EditText input = new EditText(activity);
        input.setHint(hint);
        input.setText(TextHelper.isBlank(currentValue) ? "" : currentValue.trim());

        if (multiline) {
            input.setSingleLine(false);
            input.setMaxLines(4);
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        } else {
            input.setSingleLine(true);

            if ("phoneNumber".equals(fieldKey)) {
                input.setInputType(InputType.TYPE_CLASS_PHONE);
            } else {
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
            }
        }

        int pad = ViewHelper.dp(activity, 16);
        input.setPadding(pad, pad, pad, pad);

        builder.setView(input);
        builder.setNegativeButton("Batal", null);
        builder.setPositiveButton("Simpan", null);

        AlertDialog dialog = builder.create();

        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(v -> dialog.dismiss());

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String newValue = input.getText() == null ? "" : input.getText().toString().trim();

                if (newValue.isEmpty()) {
                    AppToast.error(activity, hint + " tidak boleh kosong.");
                    input.requestFocus();
                    return;
                }

                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                submitProfileUpdate(activity, fieldKey, newValue, dialog, callback);
            });
        });

        dialog.show();
    }

    private static void submitProfileUpdate(
            BaseActivity activity,
            String fieldKey,
            String newValue,
            AlertDialog dialog,
            ProfileUpdateCallback callback
    ) {
        Map<String, String> body = new HashMap<>();
        body.put(fieldKey, newValue);

        ApiClient.getApiService(activity)
                .updateProfile(body)
                .enqueue(new Callback<ApiResponse<AuthUserResponse>>() {
                    @Override
                    public void onResponse(
                            Call<ApiResponse<AuthUserResponse>> call,
                            Response<ApiResponse<AuthUserResponse>> response
                    ) {
                        if (!response.isSuccessful()) {
                            reEnableProfileDialogButton(dialog);
                            AppToast.error(activity,
                                    ErrorParser.parseError(response, "Gagal memperbarui profil."));
                            return;
                        }

                        ApiResponse<AuthUserResponse> responseBody = response.body();

                        if (responseBody == null || !responseBody.success || responseBody.data == null) {
                            reEnableProfileDialogButton(dialog);
                            AppToast.error(activity,
                                    ErrorParser.getBestMessage(responseBody, "Gagal memperbarui profil."));
                            return;
                        }

                        if (dialog != null && dialog.isShowing()) {
                            dialog.dismiss();
                        }

                        if (callback != null) {
                            callback.onProfileUpdated(responseBody.data);
                        }

                        AppToast.success(activity, "Profil berhasil diperbarui.");
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<AuthUserResponse>> call, Throwable t) {
                        reEnableProfileDialogButton(dialog);
                        AppToast.error(activity, "Tidak bisa terhubung ke server.");
                    }
                });
    }

    private static void reEnableProfileDialogButton(AlertDialog dialog) {
        if (dialog == null || !dialog.isShowing()) {
            return;
        }

        if (dialog.getButton(AlertDialog.BUTTON_POSITIVE) != null) {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
        }
    }
}

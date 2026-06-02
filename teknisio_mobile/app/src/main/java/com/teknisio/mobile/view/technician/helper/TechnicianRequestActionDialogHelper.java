package com.teknisio.mobile.view.technician.helper;

import android.app.AlertDialog;
import android.text.InputType;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.teknisio.mobile.base.BaseActivity;
import com.teknisio.mobile.util.AppToast;
import com.teknisio.mobile.util.TextHelper;
import com.teknisio.mobile.util.ViewHelper;

import java.math.BigDecimal;

public final class TechnicianRequestActionDialogHelper {

    public interface ConfirmCallback {
        void onConfirm();
    }

    public interface RejectCallback {
        void onReject(String reason);
    }

    public interface CompleteCallback {
        void onComplete(BigDecimal finalCost, String note);
    }

    private TechnicianRequestActionDialogHelper() {
    }

    public static void confirmAccept(BaseActivity activity, ConfirmCallback callback) {
        if (activity == null) {
            return;
        }

        new AlertDialog.Builder(activity)
                .setTitle("Terima Request")
                .setMessage("Terima request servis ini?")
                .setNegativeButton("Batal", null)
                .setPositiveButton("Terima", (dialog, which) -> {
                    if (callback != null) {
                        callback.onConfirm();
                    }
                })
                .show();
    }

    public static void confirmStart(BaseActivity activity, ConfirmCallback callback) {
        if (activity == null) {
            return;
        }

        new AlertDialog.Builder(activity)
                .setTitle("Mulai Pengerjaan")
                .setMessage("Mulai pengerjaan servis ini?")
                .setNegativeButton("Batal", null)
                .setPositiveButton("Mulai", (dialog, which) -> {
                    if (callback != null) {
                        callback.onConfirm();
                    }
                })
                .show();
    }

    public static void showRejectDialog(BaseActivity activity, RejectCallback callback) {
        if (activity == null) {
            return;
        }

        final EditText input = new EditText(activity);
        input.setHint("Alasan penolakan");
        input.setSingleLine(false);
        input.setMaxLines(4);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        input.setPadding(dp(activity, 16), dp(activity, 12), dp(activity, 16), dp(activity, 12));

        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setTitle("Tolak Request")
                .setMessage("Berikan alasan penolakan agar customer memahami keputusanmu.")
                .setView(input)
                .setNegativeButton("Batal", null)
                .setPositiveButton("Tolak", null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String reason = input.getText() == null ? "" : input.getText().toString().trim();

            if (!TextHelper.isBlank(reason) && reason.length() > 1000) {
                AppToast.error(activity, "Alasan penolakan maksimal 1000 karakter.");
                input.requestFocus();
                return;
            }

            dialog.dismiss();

            if (callback != null) {
                callback.onReject(TextHelper.isBlank(reason) ? null : reason);
            }
        }));

        dialog.show();
    }

    public static void showCompleteDialog(BaseActivity activity, CompleteCallback callback) {
        if (activity == null) {
            return;
        }

        LinearLayout container = new LinearLayout(activity);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(dp(activity, 4), 0, dp(activity, 4), 0);

        final EditText edtFinalCost = new EditText(activity);
        edtFinalCost.setHint("Biaya akhir dalam Rupiah, contoh: 150000");
        edtFinalCost.setSingleLine(true);
        edtFinalCost.setInputType(InputType.TYPE_CLASS_NUMBER);
        edtFinalCost.setPadding(dp(activity, 16), dp(activity, 12), dp(activity, 16), dp(activity, 12));

        final EditText edtNote = new EditText(activity);
        edtNote.setHint("Catatan teknisi");
        edtNote.setSingleLine(false);
        edtNote.setMaxLines(4);
        edtNote.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        edtNote.setPadding(dp(activity, 16), dp(activity, 12), dp(activity, 16), dp(activity, 12));

        LinearLayout.LayoutParams noteParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        noteParams.setMargins(0, dp(activity, 12), 0, 0);
        edtNote.setLayoutParams(noteParams);

        container.addView(edtFinalCost);
        container.addView(edtNote);

        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setTitle("Selesaikan Pekerjaan")
                .setMessage("Masukkan biaya akhir dan catatan penyelesaian.")
                .setView(container)
                .setNegativeButton("Batal", null)
                .setPositiveButton("Selesaikan", null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            BigDecimal finalCost = parseFinalCost(edtFinalCost.getText() == null
                    ? ""
                    : edtFinalCost.getText().toString());

            if (finalCost == null) {
                AppToast.error(activity, "Biaya akhir wajib diisi dan harus valid.");
                edtFinalCost.requestFocus();
                return;
            }

            String note = edtNote.getText() == null ? "" : edtNote.getText().toString().trim();

            if (!TextUtils.isEmpty(note) && note.length() > 1000) {
                AppToast.error(activity, "Catatan maksimal 1000 karakter.");
                edtNote.requestFocus();
                return;
            }

            dialog.dismiss();

            if (callback != null) {
                callback.onComplete(finalCost, TextHelper.isBlank(note) ? null : note);
            }
        }));

        dialog.show();
    }

    private static BigDecimal parseFinalCost(String value) {
        if (TextHelper.isBlank(value)) {
            return null;
        }

        try {
            String normalized = value.trim().replaceAll("[^0-9]", "");

            if (TextHelper.isBlank(normalized)) {
                return null;
            }

            BigDecimal parsed = new BigDecimal(normalized);

            if (parsed.compareTo(BigDecimal.ZERO) < 0) {
                return null;
            }

            return parsed;
        } catch (Exception ignored) {
            return null;
        }
    }

    private static int dp(BaseActivity activity, int value) {
        return ViewHelper.dp(activity, value);
    }
}

package com.teknisio.mobile.util;

import android.util.Patterns;
import android.widget.EditText;

import com.teknisio.mobile.base.BaseActivity;

import java.util.regex.Pattern;

public final class InputValidator {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9]{10,15}$");

    private InputValidator() {
    }

    public static String value(EditText field) {
        if (field == null || field.getText() == null) {
            return "";
        }

        return field.getText().toString().trim();
    }

    public static String raw(EditText field) {
        if (field == null || field.getText() == null) {
            return "";
        }

        return field.getText().toString();
    }

    public static void clearErrors(EditText... fields) {
        if (fields == null) {
            return;
        }

        for (EditText field : fields) {
            if (field != null) {
                field.setError(null);
            }
        }
    }

    public static boolean requireNonBlank(BaseActivity activity, EditText field, String message) {
        if (TextHelper.isBlank(value(field))) {
            return fail(activity, field, message);
        }

        return true;
    }

    public static boolean requireLength(BaseActivity activity, EditText field, String label, int min, int max) {
        String value = value(field);

        if (value.length() < min) {
            return fail(activity, field, label + " wajib diisi.");
        }

        if (value.length() > max) {
            return fail(activity, field, label + " maksimal " + max + " karakter.");
        }

        return true;
    }

    public static boolean optionalMax(BaseActivity activity, EditText field, String label, int max) {
        String value = value(field);

        if (!TextHelper.isBlank(value) && value.length() > max) {
            return fail(activity, field, label + " maksimal " + max + " karakter.");
        }

        return true;
    }

    public static boolean requireEmail(BaseActivity activity, EditText field) {
        String email = value(field);

        if (TextHelper.isBlank(email)) {
            return fail(activity, field, "Email wajib diisi.");
        }

        if (email.length() > 100) {
            return fail(activity, field, "Email maksimal 100 karakter.");
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return fail(activity, field, "Format email tidak valid.");
        }

        return true;
    }

    public static boolean requirePhone(BaseActivity activity, EditText field) {
        String phone = value(field);

        if (TextHelper.isBlank(phone)) {
            return fail(activity, field, "Nomor telepon wajib diisi.");
        }

        if (!PHONE_PATTERN.matcher(phone).matches()) {
            return fail(activity, field, "Nomor telepon harus 10-15 digit dan boleh diawali +.");
        }

        return true;
    }

    public static boolean requirePassword(BaseActivity activity, EditText field) {
        String password = raw(field);

        if (TextHelper.isBlank(password)) {
            return fail(activity, field, "Password wajib diisi.");
        }

        if (password.length() < 8 || password.length() > 100) {
            return fail(activity, field, "Password harus 8-100 karakter.");
        }

        return true;
    }

    private static boolean fail(BaseActivity activity, EditText field, String message) {
        if (field != null) {
            field.setError(message);
            field.requestFocus();
        }

        if (activity != null) {
            AppToast.error(activity, message);
        }

        return false;
    }
}

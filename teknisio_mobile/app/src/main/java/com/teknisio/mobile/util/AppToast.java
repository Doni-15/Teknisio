package com.teknisio.mobile.util;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

public final class AppToast {

    private AppToast() {
    }

    public static void success(Activity activity, String message) {
        show(activity, message, "#2E7D32");
    }

    public static void error(Activity activity, String message) {
        show(activity, message, "#C62828");
    }

    public static void warning(Activity activity, String message) {
        show(activity, message, "#F6A800");
    }

    public static void info(Activity activity, String message) {
        show(activity, message, "#2F4A8A");
    }

    private static void show(Activity activity, String message, String color) {
        if (activity == null || activity.isFinishing()) {
            return;
        }

        if (message == null || message.trim().isEmpty()) {
            return;
        }

        activity.runOnUiThread(() -> {
            try {
                View root = activity.findViewById(android.R.id.content);

                if (root == null) {
                    Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
                    return;
                }

                Snackbar snackbar = Snackbar.make(root, message.trim(), Snackbar.LENGTH_LONG);
                snackbar.setBackgroundTint(Color.parseColor(color));
                snackbar.setTextColor(Color.WHITE);
                snackbar.show();
            } catch (Exception exception) {
                Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
            }
        });
    }
}

package com.teknisio.mobile.util;

import android.app.Activity;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;

public final class SystemBarHelper {

    private SystemBarHelper() {
    }

    public static void hideNavigationBar(Activity activity) {
        if (activity == null || activity.isFinishing()) {
            return;
        }

        Window window = activity.getWindow();

        if (window == null) {
            return;
        }

        View decorView = window.getDecorView();

        if (decorView == null) {
            return;
        }

        decorView.post(() -> {
            try {
                if (activity.isFinishing()) {
                    return;
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    WindowInsetsController controller = window.getInsetsController();

                    if (controller != null) {
                        controller.hide(WindowInsets.Type.navigationBars());
                        controller.setSystemBarsBehavior(
                                WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                        );
                    }

                    return;
                }

                decorView.setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                );
            } catch (Exception ignored) {
                // System bar handling must never crash the app.
            }
        });
    }
}

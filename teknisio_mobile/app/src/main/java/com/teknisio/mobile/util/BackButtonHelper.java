package com.teknisio.mobile.util;

import android.widget.FrameLayout;

public final class BackButtonHelper {

    private BackButtonHelper() {
    }

    public static void setup(FrameLayout btnBack, Runnable action) {
        if (btnBack == null) {
            return;
        }

        btnBack.setOnClickListener(v -> {
            if (action != null) {
                action.run();
            }
        });
    }
}

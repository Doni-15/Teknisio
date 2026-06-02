package com.teknisio.mobile.util;

import android.content.Context;
import android.content.SharedPreferences;

public final class ReviewStateStore {

    private static final String PREF_NAME = "review_state";

    private ReviewStateStore() {
    }

    public static boolean isReviewed(Context context, String serviceRequestId) {
        if (context == null || TextHelper.isBlank(serviceRequestId)) {
            return false;
        }

        return getPrefs(context).getBoolean(getKey(serviceRequestId), false);
    }

    public static void markReviewed(Context context, String serviceRequestId) {
        if (context == null || TextHelper.isBlank(serviceRequestId)) {
            return;
        }

        getPrefs(context)
                .edit()
                .putBoolean(getKey(serviceRequestId), true)
                .apply();
    }

    private static SharedPreferences getPrefs(Context context) {
        return context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    private static String getKey(String serviceRequestId) {
        return "reviewed_" + serviceRequestId.trim();
    }
}

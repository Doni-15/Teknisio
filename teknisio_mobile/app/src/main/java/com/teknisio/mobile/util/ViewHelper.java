package com.teknisio.mobile.util;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;

public final class ViewHelper {

    private ViewHelper() {
    }

    public static int dp(Context context, int value) {
        if (context == null) {
            return value;
        }

        return (int) (value * context.getResources().getDisplayMetrics().density + 0.5f);
    }

    public static GradientDrawable rounded(Context context, String color, int radiusDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Color.parseColor(color));
        drawable.setCornerRadius(dp(context, radiusDp));
        return drawable;
    }

    public static GradientDrawable strokeRounded(
            Context context,
            String fillColor,
            String strokeColor,
            int radiusDp,
            int strokeWidthDp
    ) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Color.parseColor(fillColor));
        drawable.setCornerRadius(dp(context, radiusDp));
        drawable.setStroke(dp(context, strokeWidthDp), Color.parseColor(strokeColor));
        return drawable;
    }

    public static GradientDrawable oval(String color) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setColor(Color.parseColor(color));
        return drawable;
    }
}

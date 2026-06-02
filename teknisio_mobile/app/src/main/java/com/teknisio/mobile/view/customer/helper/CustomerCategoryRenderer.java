package com.teknisio.mobile.view.customer.helper;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.teknisio.mobile.R;
import com.teknisio.mobile.base.BaseActivity;
import com.teknisio.mobile.model.response.DeviceCategoryResponse;
import com.teknisio.mobile.util.TextHelper;
import com.teknisio.mobile.util.ViewHelper;

import java.util.List;

public final class CustomerCategoryRenderer {

    public interface CategoryClickListener {
        void onCategoryClick(DeviceCategoryResponse category);
    }

    private CustomerCategoryRenderer() {
    }

    public static void render(
            BaseActivity activity,
            GridLayout gridCategories,
            TextView txtCategoryEmpty,
            List<DeviceCategoryResponse> categories,
            String selectedCategoryId,
            CategoryClickListener listener
    ) {
        if (activity == null || gridCategories == null || txtCategoryEmpty == null) {
            return;
        }

        txtCategoryEmpty.setVisibility(android.view.View.GONE);
        gridCategories.setVisibility(android.view.View.VISIBLE);
        gridCategories.removeAllViews();

        int total = categories == null ? 0 : categories.size();
        int maxItems = Math.min(total, 8);

        for (int i = 0; i < maxItems; i++) {
            DeviceCategoryResponse category = categories.get(i);
            gridCategories.addView(createCategoryItem(activity, category, selectedCategoryId, listener));
        }
    }

    private static LinearLayout createCategoryItem(
            BaseActivity activity,
            DeviceCategoryResponse category,
            String selectedCategoryId,
            CategoryClickListener listener
    ) {
        boolean selected = category != null
                && category.deviceCategoryId != null
                && category.deviceCategoryId.equals(selectedCategoryId);

        LinearLayout item = new LinearLayout(activity);
        item.setOrientation(LinearLayout.VERTICAL);
        item.setGravity(Gravity.CENTER);
        item.setPadding(dp(activity, 4), dp(activity, 8), dp(activity, 4), dp(activity, 8));

        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        params.setMargins(dp(activity, 2), dp(activity, 4), dp(activity, 2), dp(activity, 8));
        item.setLayoutParams(params);

        FrameLayout iconCircle = new FrameLayout(activity);
        LinearLayout.LayoutParams iconCircleParams = new LinearLayout.LayoutParams(
                dp(activity, 54),
                dp(activity, 54)
        );
        iconCircle.setLayoutParams(iconCircleParams);
        iconCircle.setBackground(makeOval(activity, selected ? "#C8F1F5" : "#DDF8FA"));

        int iconResId = getCategoryIconResId(category == null ? null : category.name);

        if (iconResId != 0) {
            ImageView iconImage = new ImageView(activity);

            FrameLayout.LayoutParams imageParams = new FrameLayout.LayoutParams(
                    dp(activity, 31),
                    dp(activity, 31),
                    Gravity.CENTER
            );

            iconImage.setLayoutParams(imageParams);
            iconImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
            iconImage.setImageResource(iconResId);

            iconCircle.addView(iconImage);
        } else {
            TextView fallbackIcon = new TextView(activity);

            FrameLayout.LayoutParams fallbackParams = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            );

            fallbackIcon.setLayoutParams(fallbackParams);
            fallbackIcon.setGravity(Gravity.CENTER);
            fallbackIcon.setText(getCategoryShortName(category == null ? null : category.name));
            fallbackIcon.setTextColor(Color.parseColor("#2F4A8A"));
            fallbackIcon.setTextSize(13);
            fallbackIcon.setTypeface(Typeface.DEFAULT_BOLD);

            iconCircle.addView(fallbackIcon);
        }

        TextView name = new TextView(activity);
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        nameParams.setMargins(0, dp(activity, 9), 0, 0);
        name.setLayoutParams(nameParams);
        name.setGravity(Gravity.CENTER);
        name.setText(getCategoryDisplayName(category == null ? null : category.name));
        name.setTextColor(selected ? Color.parseColor("#2F4A8A") : Color.parseColor("#1F2329"));
        name.setTextSize(13);
        name.setTypeface(selected ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        name.setMaxLines(2);
        name.setEllipsize(TextUtils.TruncateAt.END);

        item.addView(iconCircle);
        item.addView(name);

        item.setOnClickListener(v -> {
            if (category == null || TextHelper.isBlank(category.deviceCategoryId)) {
                return;
            }

            if (listener != null) {
                listener.onCategoryClick(category);
            }
        });

        return item;
    }

    public static String getCategoryDisplayName(String name) {
        if (TextHelper.isBlank(name)) {
            return "-";
        }

        String lower = name.toLowerCase().trim();

        if (lower.equals("ac")
                || lower.contains("air conditioner")
                || lower.contains("pendingin")) {
            return "AC";
        }

        if (lower.contains("washing") || lower.contains("wash") || lower.contains("cuci")) {
            return "Washing\nMachine";
        }

        if (lower.contains("rice") || lower.contains("nasi")) {
            return "Rice\nCooker";
        }

        if (lower.contains("fridge")
                || lower.contains("refrigerator")
                || lower.contains("kulkas")) {
            return "Fridge";
        }

        if (lower.contains("television") || lower.equals("tv") || lower.contains("televisi")) {
            return "TV";
        }

        if (lower.contains("oven")) {
            return "Oven";
        }

        if (lower.contains("fan") || lower.contains("kipas")) {
            return "Fan";
        }

        if (lower.contains("mixer")) {
            return "Mixer";
        }

        return name.trim();
    }

    private static int getCategoryIconResId(String name) {
        if (TextHelper.isBlank(name)) {
            return 0;
        }

        String lower = name.toLowerCase().trim();

        if (lower.contains("washing") || lower.contains("wash") || lower.contains("cuci")) {
            return R.drawable.washing_machine;
        }

        if (lower.contains("rice") || lower.contains("nasi")) {
            return R.drawable.rice_cooker;
        }

        if (lower.contains("fridge")
                || lower.contains("refrigerator")
                || lower.contains("kulkas")) {
            return R.drawable.refrigerator;
        }

        if (lower.contains("oven")) {
            return R.drawable.oven;
        }

        if (lower.contains("television") || lower.equals("tv") || lower.contains("televisi")) {
            return R.drawable.television;
        }

        if (lower.contains("fan") || lower.contains("kipas")) {
            return R.drawable.fan;
        }

        if (lower.contains("mixer")) {
            return R.drawable.mixer;
        }

        if (lower.equals("ac")
                || lower.contains("air conditioner")
                || lower.contains("pendingin")) {
            return R.drawable.ac;
        }

        return 0;
    }

    private static String getCategoryShortName(String name) {
        if (TextHelper.isBlank(name)) {
            return "DV";
        }

        String lower = name.toLowerCase();

        if (lower.contains("air") || lower.contains("ac")) return "AC";
        if (lower.contains("tv") || lower.contains("television")) return "TV";
        if (lower.contains("fan")) return "FN";
        if (lower.contains("rice")) return "RC";
        if (lower.contains("wash")) return "WM";
        if (lower.contains("fridge") || lower.contains("kulkas")) return "FR";
        if (lower.contains("oven")) return "OV";

        String[] parts = name.trim().split("\\s+");

        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }

        String first = parts[0].substring(0, 1);
        String second = parts[1].substring(0, 1);

        return (first + second).toUpperCase();
    }

    private static GradientDrawable makeOval(BaseActivity activity, String color) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setColor(Color.parseColor(color));
        return drawable;
    }

    private static int dp(BaseActivity activity, int value) {
        return ViewHelper.dp(activity, value);
    }
}

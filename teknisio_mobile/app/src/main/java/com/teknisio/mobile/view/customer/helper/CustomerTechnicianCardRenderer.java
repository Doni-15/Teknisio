package com.teknisio.mobile.view.customer.helper;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.teknisio.mobile.base.BaseActivity;
import com.teknisio.mobile.model.response.CustomerTechnicianResponse;
import com.teknisio.mobile.model.response.DeviceCategoryResponse;
import com.teknisio.mobile.util.TextHelper;
import com.teknisio.mobile.util.TechnicianAvailabilityHelper;
import com.teknisio.mobile.util.ViewHelper;

import java.math.BigDecimal;

public final class CustomerTechnicianCardRenderer {

    public interface TechnicianClickListener {
        void onTechnicianClick(CustomerTechnicianResponse technician);
    }

    private CustomerTechnicianCardRenderer() {
    }

    public static LinearLayout create(
            BaseActivity activity,
            CustomerTechnicianResponse technician,
            TechnicianClickListener listener
    ) {
        LinearLayout card = new LinearLayout(activity);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(activity, 14), dp(activity, 14), dp(activity, 14), dp(activity, 14));
        card.setBackground(makeStrokeRounded("#FFFFFF", "#DCE6EB", 18, 1));
        card.setElevation(dp(activity, 2));

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                dp(activity, 240),
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, dp(activity, 14), dp(activity, 6));
        card.setLayoutParams(cardParams);

        LinearLayout topRow = new LinearLayout(activity);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView avatar = new TextView(activity);
        LinearLayout.LayoutParams avatarParams = new LinearLayout.LayoutParams(
                dp(activity, 52),
                dp(activity, 52)
        );
        avatar.setLayoutParams(avatarParams);
        avatar.setGravity(Gravity.CENTER);
        avatar.setBackground(makeOval("#EAF4FF"));
        avatar.setText(getInitial(technician == null ? null : technician.name));
        avatar.setTextColor(Color.parseColor("#2F4A8A"));
        avatar.setTextSize(22);
        avatar.setTypeface(Typeface.DEFAULT_BOLD);

        LinearLayout infoWrap = new LinearLayout(activity);
        LinearLayout.LayoutParams infoWrapParams = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
        );
        infoWrapParams.setMargins(dp(activity, 12), 0, dp(activity, 8), 0);
        infoWrap.setLayoutParams(infoWrapParams);
        infoWrap.setOrientation(LinearLayout.VERTICAL);

        TextView name = new TextView(activity);
        name.setText(technician == null || technician.name == null ? "Teknisi" : technician.name);
        name.setTextColor(Color.parseColor("#1F2329"));
        name.setTextSize(17);
        name.setTypeface(Typeface.DEFAULT_BOLD);
        name.setMaxLines(1);
        name.setEllipsize(TextUtils.TruncateAt.END);

        TextView skill = new TextView(activity);
        skill.setText(getTechnicianSubtext(technician));
        skill.setTextColor(Color.parseColor("#6B7680"));
        skill.setTextSize(13);
        skill.setMaxLines(2);
        skill.setEllipsize(TextUtils.TruncateAt.END);

        infoWrap.addView(name);
        infoWrap.addView(skill);

        TextView ratingChip = new TextView(activity);
        ratingChip.setPadding(dp(activity, 10), dp(activity, 5), dp(activity, 10), dp(activity, 5));
        ratingChip.setBackground(makeStrokeRounded("#F5FAFF", "#D6E4F0", 12, 1));
        ratingChip.setText("★ " + formatRating(technician == null ? null : technician.averageRating));
        ratingChip.setTextColor(Color.parseColor("#2F4A8A"));
        ratingChip.setTextSize(12);
        ratingChip.setTypeface(Typeface.DEFAULT_BOLD);

        topRow.addView(avatar);
        topRow.addView(infoWrap);
        topRow.addView(ratingChip);

        TextView spacer = new TextView(activity);
        spacer.setHeight(dp(activity, 10));

        LinearLayout statsRow = new LinearLayout(activity);
        statsRow.setOrientation(LinearLayout.HORIZONTAL);
        statsRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView jobsChip = new TextView(activity);
        jobsChip.setPadding(dp(activity, 10), dp(activity, 5), dp(activity, 10), dp(activity, 5));
        jobsChip.setBackground(makeStrokeRounded("#F8FBFC", "#E1EAEE", 12, 1));
        jobsChip.setText(formatJobs(technician == null ? null : technician.totalJobs));
        jobsChip.setTextColor(Color.parseColor("#5F6B73"));
        jobsChip.setTextSize(12);

        TextView statusChip = new TextView(activity);
        LinearLayout.LayoutParams statusParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        statusParams.setMargins(dp(activity, 8), 0, 0, 0);
        statusChip.setLayoutParams(statusParams);
        statusChip.setPadding(dp(activity, 10), dp(activity, 5), dp(activity, 10), dp(activity, 5));
        String status = technician == null ? null : technician.availabilityStatus;
        boolean online = TechnicianAvailabilityHelper.isOnline(status);
        statusChip.setBackground(makeStrokeRounded(
                online ? "#EDF9F0" : "#F3F7F9",
                online ? "#CFE9D6" : "#DCE6EB",
                12,
                1
        ));
        statusChip.setText(TechnicianAvailabilityHelper.toDisplayText(status));
        statusChip.setTextColor(Color.parseColor(online ? "#2E7D32" : "#6B7680"));
        statusChip.setTextSize(12);

        statsRow.addView(jobsChip);
        statsRow.addView(statusChip);

        TextView button = new TextView(activity);
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(activity, 38)
        );
        buttonParams.setMargins(0, dp(activity, 12), 0, 0);
        button.setLayoutParams(buttonParams);
        button.setGravity(Gravity.CENTER);
        button.setBackground(makeRounded("#445D9B", 12));
        button.setText("More Info");
        button.setTextColor(Color.WHITE);
        button.setTextSize(13);
        button.setTypeface(Typeface.DEFAULT_BOLD);

        card.addView(topRow);
        card.addView(spacer);
        card.addView(statsRow);
        card.addView(button);

        button.setOnClickListener(v -> notifyClick(listener, technician));
        card.setOnClickListener(v -> notifyClick(listener, technician));

        return card;
    }

    private static void notifyClick(
            TechnicianClickListener listener,
            CustomerTechnicianResponse technician
    ) {
        if (listener != null) {
            listener.onTechnicianClick(technician);
        }
    }

    private static String getTechnicianSubtext(CustomerTechnicianResponse technician) {
        if (technician == null) {
            return "Teknisi terdaftar";
        }

        if (technician.supportedDeviceCategories != null && !technician.supportedDeviceCategories.isEmpty()) {
            StringBuilder builder = new StringBuilder();

            int limit = Math.min(technician.supportedDeviceCategories.size(), 2);

            for (int i = 0; i < limit; i++) {
                DeviceCategoryResponse category = technician.supportedDeviceCategories.get(i);

                if (category != null && category.name != null) {
                    if (builder.length() > 0) {
                        builder.append(" • ");
                    }

                    builder.append(category.name);
                }
            }

            if (builder.length() > 0) {
                return builder.toString();
            }
        }

        if (technician.description != null && !technician.description.trim().isEmpty()) {
            return technician.description.trim();
        }

        return "Teknisi terdaftar";
    }

    private static String formatRating(BigDecimal rating) {
        if (rating == null) {
            return "0.0";
        }

        return rating.setScale(1, java.math.RoundingMode.HALF_UP).toPlainString();
    }

    private static String formatJobs(Integer totalJobs) {
        int jobs = totalJobs == null ? 0 : totalJobs;
        return jobs + " pekerjaan";
    }

    private static String getInitial(String name) {
        if (TextHelper.isBlank(name)) {
            return "T";
        }

        return name.trim().substring(0, 1).toUpperCase();
    }

    private static GradientDrawable makeRounded(String color, int radiusDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Color.parseColor(color));
        drawable.setCornerRadius(radiusDp);
        return drawable;
    }

    private static GradientDrawable makeStrokeRounded(
            String fillColor,
            String strokeColor,
            int radiusDp,
            int strokeWidthDp
    ) {
        GradientDrawable drawable = makeRounded(fillColor, radiusDp);
        drawable.setStroke(strokeWidthDp, Color.parseColor(strokeColor));
        return drawable;
    }

    private static GradientDrawable makeOval(String color) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setColor(Color.parseColor(color));
        return drawable;
    }

    private static int dp(BaseActivity activity, int value) {
        return ViewHelper.dp(activity, value);
    }
}

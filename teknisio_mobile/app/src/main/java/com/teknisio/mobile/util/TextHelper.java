package com.teknisio.mobile.util;

import com.teknisio.mobile.model.response.DeviceCategoryResponse;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public final class TextHelper {

    private TextHelper() {
    }

    public static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static String safe(String value, String fallback) {
        return isBlank(value) ? fallback : value.trim();
    }

    public static String initial(String value, String fallback) {
        if (isBlank(value)) {
            return fallback;
        }

        String[] parts = value.trim().split("\\s+");

        if (parts.length == 1) {
            return parts[0]
                    .substring(0, Math.min(2, parts[0].length()))
                    .toUpperCase();
        }

        return (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase();
    }

    public static String rating(BigDecimal rating) {
        if (rating == null) {
            return "0.0";
        }

        return rating.setScale(1, RoundingMode.HALF_UP).toPlainString();
    }

    public static String jobs(Integer totalJobs) {
        int jobs = totalJobs == null ? 0 : totalJobs;
        return jobs + " pekerjaan";
    }

    public static String number(Integer value) {
        return String.valueOf(value == null ? 0 : value);
    }

    public static String deviceCategoriesText(
            List<DeviceCategoryResponse> categories,
            String fallback,
            String delimiter
    ) {
        if (categories == null || categories.isEmpty()) {
            return fallback;
        }

        StringBuilder builder = new StringBuilder();

        for (DeviceCategoryResponse category : categories) {
            if (category == null || isBlank(category.name)) {
                continue;
            }

            if (builder.length() > 0) {
                builder.append(delimiter);
            }

            builder.append(category.name.trim());
        }

        return builder.length() == 0 ? fallback : builder.toString();
    }

    public static String formatDateTime(String isoTime) {
        if (isBlank(isoTime)) {
            return "-";
        }
        try {
            java.util.Date date = null;
            String cleanTime = isoTime.trim();

            // Support multiple ISO formats for cross-compatibility
            String[] formats = {
                "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
                "yyyy-MM-dd'T'HH:mm:ssXXX",
                "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
                "yyyy-MM-dd'T'HH:mm:ssZ",
                "yyyy-MM-dd'T'HH:mm:ss.SSS",
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd HH:mm:ss"
            };

            for (String format : formats) {
                try {
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(format, java.util.Locale.US);
                    date = sdf.parse(cleanTime);
                    break;
                } catch (Exception ignored) {}
            }

            if (date == null) {
                return isoTime;
            }

            java.text.SimpleDateFormat displayFormat = new java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", new java.util.Locale("id", "ID"));
            return displayFormat.format(date);
        } catch (Exception e) {
            return isoTime;
        }
    }
}

package com.teknisio.mobile.util;

public final class OrderStatusHelper {

    private OrderStatusHelper() {
    }

    public static boolean canCancel(String status) {
        String normalized = normalize(status);

        return "WAITING".equals(normalized)
                || "ACCEPTED".equals(normalized)
                || "ON_PROGRESS".equals(normalized);
    }

    public static String normalize(String status) {
        if (status == null) {
            return "";
        }

        return status.trim().toUpperCase();
    }

    public static String getDisplayStatus(String status) {
        String normalized = normalize(status);

        if ("WAITING".equals(normalized)) return "Waiting";
        if ("ACCEPTED".equals(normalized)) return "Accepted";
        if ("ON_PROGRESS".equals(normalized)) return "On Progress";
        if ("COMPLETED".equals(normalized)) return "Completed";
        if ("CANCELLED".equals(normalized)) return "Cancelled";
        if ("REJECTED".equals(normalized)) return "Rejected";

        if (normalized.isEmpty()) {
            return "Unknown";
        }

        return normalized.replace("_", " ");
    }

    public static String getStatusColor(String status) {
        String normalized = normalize(status);

        if ("WAITING".equals(normalized)) return "#F6A800";
        if ("ACCEPTED".equals(normalized)) return "#2F4A8A";
        if ("ON_PROGRESS".equals(normalized)) return "#0097A7";
        if ("COMPLETED".equals(normalized)) return "#2E7D32";
        if ("CANCELLED".equals(normalized)) return "#7A7F85";
        if ("REJECTED".equals(normalized)) return "#C62828";

        return "#6B7680";
    }
}

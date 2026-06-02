package com.teknisio.mobile.util;

public final class TechnicianAvailabilityHelper {

    public static final String STATUS_ONLINE = "ONLINE";

    private TechnicianAvailabilityHelper() {
    }

    public static boolean isOnline(String status) {
        return STATUS_ONLINE.equals(normalize(status));
    }

    public static String toDisplayText(String status) {
        String normalized = normalize(status);

        if (TextHelper.isBlank(normalized)) {
            return "Tidak diketahui";
        }

        switch (normalized) {
            case "ONLINE":
                return "Tersedia";
            case "OFFLINE":
                return "Offline";
            case "BUSY":
                return "Sibuk";
            case "ON_LEAVE":
                return "Cuti";
            default:
                return status == null ? "Tidak diketahui" : status.trim().replace("_", " ");
        }
    }

    public static String normalize(String status) {
        if (TextHelper.isBlank(status)) {
            return "";
        }

        return status.trim()
                .replace('-', '_')
                .replace(' ', '_')
                .toUpperCase(java.util.Locale.ROOT);
    }
}

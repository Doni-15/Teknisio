package com.teknisio.mobile.model.response;

public class NotificationResponse {
    public String notificationId;
    public String id;

    public String title;
    public String message;
    public String body;

    public String type;
    public String category;

    public String referenceType;
    public String referenceId;
    public String serviceRequestId;

    public Boolean read;
    public Boolean isRead;
    public String readAt;

    public String createdAt;
    public String updatedAt;

    public String getStableId() {
        if (!isBlank(notificationId)) {
            return notificationId;
        }

        return id;
    }

    public String getDisplayTitle() {
        if (!isBlank(title)) {
            return title;
        }

        if (!isBlank(type)) {
            return formatType(type);
        }

        if (!isBlank(category)) {
            return formatType(category);
        }

        return "Notifikasi Teknisio";
    }

    public String getDisplayMessage() {
        if (!isBlank(message)) {
            return message;
        }

        if (!isBlank(body)) {
            return body;
        }

        return "Ada pembaruan dari sistem.";
    }

    public String getDisplayTime() {
        if (!isBlank(createdAt)) {
            return createdAt;
        }

        if (!isBlank(updatedAt)) {
            return updatedAt;
        }

        return "";
    }

    public boolean isUnread() {
        if (!isBlank(readAt)) {
            return false;
        }

        if (Boolean.TRUE.equals(read) || Boolean.TRUE.equals(isRead)) {
            return false;
        }

        return true;
    }

    public void markReadLocal() {
        this.read = true;
        this.isRead = true;
        this.readAt = "read";
    }

    private String formatType(String raw) {
        String value = raw.trim().replace("_", " ").replace("-", " ").toLowerCase();

        if (value.isEmpty()) {
            return "Notifikasi Teknisio";
        }

        return value.substring(0, 1).toUpperCase() + value.substring(1);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}

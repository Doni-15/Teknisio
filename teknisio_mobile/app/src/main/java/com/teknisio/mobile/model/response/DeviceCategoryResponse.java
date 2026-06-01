package com.teknisio.mobile.model.response;

public class DeviceCategoryResponse {
    public String deviceCategoryId;
    public String name;
    public String icon;

    // Ada pada response technician skill.
    // Tidak masalah kalau response public /api/device-categories tidak mengirim field ini.
    public Boolean active;
}

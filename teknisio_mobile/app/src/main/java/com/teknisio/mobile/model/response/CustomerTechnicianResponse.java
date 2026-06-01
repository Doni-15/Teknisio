package com.teknisio.mobile.model.response;

import java.math.BigDecimal;
import java.util.List;

public class CustomerTechnicianResponse {
    public String technicianProfileId;
    public String name;
    public String profilePhoto;
    public String availabilityStatus;
    public BigDecimal averageRating;
    public Integer ratingCount;
    public Integer totalJobs;
    public String description;
    public List<DeviceCategoryResponse> supportedDeviceCategories;
}

package com.teknisio.mobile.model.response;

import java.math.BigDecimal;
import java.util.List;

public class ServiceRequestResponse {
    public String serviceRequestId;
    public String serviceRequestCode;

    public String customerId;
    public String customerName;
    public String customerPhoneNumber;
    public String customerProfilePhoto;

    public String technicianProfileId;
    public String status;

    public String issueDescription;
    public String address;
    public String addressDetail;

    public BigDecimal estimatedCost;
    public BigDecimal finalCost;
    public String technicianNote;

    public String cancelReason;
    public String rejectReason;

    public List<DeviceCategoryResponse> selectedDeviceCategories;

    public String requestTime;
    public String acceptedAt;
    public String startedAt;
    public String completedAt;
    public String cancelledAt;
    public String rejectedAt;
}

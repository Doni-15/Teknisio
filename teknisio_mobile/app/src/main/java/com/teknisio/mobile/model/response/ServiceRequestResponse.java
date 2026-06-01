package com.teknisio.mobile.model.response;

import java.util.List;

public class ServiceRequestResponse {
    public String serviceRequestId;
    public String serviceRequestCode;
    public String customerId;
    public String technicianProfileId;
    public String status;
    public String issueDescription;
    public String address;
    public String addressDetail;
    public String cancelReason;
    public List<DeviceCategoryResponse> selectedDeviceCategories;
    public String requestTime;
    public String cancelledAt;
}

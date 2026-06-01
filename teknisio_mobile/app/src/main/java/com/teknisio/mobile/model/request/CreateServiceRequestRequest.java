package com.teknisio.mobile.model.request;

import java.util.List;

public class CreateServiceRequestRequest {
    public String technicianProfileId;
    public List<String> deviceCategoryIds;
    public String issueDescription;
    public String address;
    public String addressDetail;

    public CreateServiceRequestRequest(
            String technicianProfileId,
            List<String> deviceCategoryIds,
            String issueDescription,
            String address,
            String addressDetail
    ) {
        this.technicianProfileId = technicianProfileId;
        this.deviceCategoryIds = deviceCategoryIds;
        this.issueDescription = issueDescription;
        this.address = address;
        this.addressDetail = addressDetail;
    }
}

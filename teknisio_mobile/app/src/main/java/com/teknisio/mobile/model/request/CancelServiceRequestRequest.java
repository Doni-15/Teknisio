package com.teknisio.mobile.model.request;

public class CancelServiceRequestRequest {
    public String cancelReason;

    public CancelServiceRequestRequest(String cancelReason) {
        this.cancelReason = cancelReason;
    }
}

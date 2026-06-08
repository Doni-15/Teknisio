package com.teknisio.mobile.model.request;

public class SendChatRequest {
    public String serviceRequestId;
    public String message;

    public SendChatRequest(String serviceRequestId, String message) {
        this.serviceRequestId = serviceRequestId;
        this.message = message;
    }
}
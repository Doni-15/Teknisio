package com.teknisio.mobile.model.response;

public class ChatMessageResponse {
    public String chatId;
    public String serviceRequestId;
    public String senderId;
    public String senderName;
    public String receiverId;
    public String message;

    // Backend bisa mengirim "isRead" atau "read" tergantung serializer boolean.
    public Boolean isRead;
    public Boolean read;

    public String sentAt;

    public boolean isReadFromBackend() {
        return Boolean.TRUE.equals(isRead) || Boolean.TRUE.equals(read);
    }
}

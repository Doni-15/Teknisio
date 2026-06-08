package com.teknisio.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageResponse {

  private String chatId;
  private String serviceRequestId;
  private String senderId;
  private String senderName;
  private String receiverId;
  private String message;
  private boolean isRead;
  private String sentAt;
}
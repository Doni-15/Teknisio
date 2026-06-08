package com.teknisio.dto.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageRequest {

  @NotBlank(message = "Service request ID is required")
  private String serviceRequestId;

  @NotBlank(message = "Message is required")
  @Size(max = 2000, message = "Message must not exceed 2000 characters")
  private String message;
}
package com.teknisio.controllers;

import com.teknisio.common.response.ApiResponse;
import com.teknisio.dto.requests.ChatMessageRequest;
import com.teknisio.dto.responses.ChatMessageResponse;
import com.teknisio.security.CustomUserDetails;
import com.teknisio.services.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

  private final ChatService chatService;

  @MessageMapping("/chat/send/{serviceRequestId}")
  public void sendChatMessage(
          @DestinationVariable String serviceRequestId,
          @Payload @Valid ChatMessageRequest request,
          Principal principal
  ) {
    if (principal == null) {
      log.error("STOMP message rejected: principal is null");
      throw new IllegalStateException("User not authenticated");
    }

    CustomUserDetails userDetails = null;
    if (principal instanceof UsernamePasswordAuthenticationToken) {
      userDetails = (CustomUserDetails) ((UsernamePasswordAuthenticationToken) principal).getPrincipal();
    }

    if (userDetails == null) {
      log.error("STOMP message rejected: userDetails is null");
      throw new IllegalStateException("User not authenticated");
    }

    request.setServiceRequestId(serviceRequestId);
    UUID senderId = userDetails.getIdUser();
    chatService.sendMessage(senderId, request);
  }

  @GetMapping("/{serviceRequestId}")
  public ResponseEntity<ApiResponse<List<ChatMessageResponse>>> getChatHistory(
          @PathVariable String serviceRequestId,
          @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    UUID id = UUID.fromString(serviceRequestId);
    List<ChatMessageResponse> history = chatService.getChatHistory(id, userDetails.getIdUser());
    return ResponseEntity.ok(ApiResponse.success("Chat history retrieved", history));
  }

  @PatchMapping("/{serviceRequestId}/read")
  public ResponseEntity<ApiResponse<Void>> markAsRead(
          @PathVariable String serviceRequestId,
          @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    UUID requestId = UUID.fromString(serviceRequestId);
    UUID userId = userDetails.getIdUser();
    chatService.markAsRead(requestId, userId);
    return ResponseEntity.ok(ApiResponse.success("Messages marked as read", null));
  }

  @GetMapping("/unread-count")
  public ResponseEntity<ApiResponse<Long>> getUnreadCount(
          @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    UUID userId = userDetails.getIdUser();
    long count = chatService.getUnreadCount(userId);
    return ResponseEntity.ok(ApiResponse.success("Unread count retrieved", count));
  }
}

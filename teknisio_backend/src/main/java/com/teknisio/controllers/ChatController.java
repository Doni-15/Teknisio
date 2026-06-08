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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.security.Principal;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

/**
 * Handles chat messaging between customer and technician for a service request.
 *
 * <p><b>STOMP (real-time):</b><br>
 * Send: {@code /app/chat/send/{serviceRequestId}}<br>
 * Receive: {@code /topic/chat/{serviceRequestId}}
 *
 * <p><b>REST (history + read status):</b><br>
 * {@code GET /api/chat/{serviceRequestId}}<br>
 * {@code PATCH /api/chat/{serviceRequestId}/read}<br>
 * {@code GET /api/chat/unread-count}
 */
@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

  private final ChatService chatService;

  // ─────────────────────────────────────────────────────────────────────────
  // STOMP — real-time message sending
  // ─────────────────────────────────────────────────────────────────────────

  /**
   * Receives a chat message via STOMP from either customer or technician.
   * Stores it and broadcasts to {@code /topic/chat/{serviceRequestId}}.
   */
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

    // Override the serviceRequestId from the STOMP path for consistency
    request.setServiceRequestId(serviceRequestId);
    UUID senderId = userDetails.getIdUser();
    chatService.sendMessage(senderId, request);
  }

  // ─────────────────────────────────────────────────────────────────────────
  // REST — chat history
  // ─────────────────────────────────────────────────────────────────────────

  /**
   * Returns the full chat history for a given service request, ordered chronologically.
   */
  @GetMapping("/{serviceRequestId}")
  public ResponseEntity<ApiResponse<List<ChatMessageResponse>>> getChatHistory(
          @PathVariable String serviceRequestId
  ) {
    UUID id = UUID.fromString(serviceRequestId);
    List<ChatMessageResponse> history = chatService.getChatHistory(id);
    return ResponseEntity.ok(ApiResponse.success("Chat history retrieved", history));
  }

  /**
   * Marks all messages in a service request as read for the current user.
   */
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

  /**
   * Returns the count of unread messages for the current user.
   */
  @GetMapping("/unread-count")
  public ResponseEntity<ApiResponse<Long>> getUnreadCount(
          @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    UUID userId = userDetails.getIdUser();
    long count = chatService.getUnreadCount(userId);
    return ResponseEntity.ok(ApiResponse.success("Unread count retrieved", count));
  }
}
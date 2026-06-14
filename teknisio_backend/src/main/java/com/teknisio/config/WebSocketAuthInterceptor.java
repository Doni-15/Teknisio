package com.teknisio.config;

import com.teknisio.security.CustomUserDetails;
import com.teknisio.security.CustomUserDetailsService;
import com.teknisio.security.JwtService;
import com.teknisio.services.WebSocketAuthorizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

  private final JwtService jwtService;
  private final CustomUserDetailsService customUserDetailsService;
  private final WebSocketAuthorizationService webSocketAuthorizationService;

  private static final String BEARER_PREFIX = "Bearer ";

  @Override
  public Message<?> preSend(Message<?> message, MessageChannel channel) {
    StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

    if (accessor == null || accessor.getCommand() == null) {
      return message;
    }

    StompCommand command = accessor.getCommand();

    if (StompCommand.CONNECT.equals(command)) {
      return handleConnect(message, accessor);
    }

    if (StompCommand.SEND.equals(command) || StompCommand.SUBSCRIBE.equals(command)) {
      return handleProtectedFrame(message, accessor);
    }

    return message;
  }

  private Message<?> handleConnect(Message<?> message, StompHeaderAccessor accessor) {
    String authHeader = getAuthorizationHeader(accessor);

    if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
      log.warn("WebSocket CONNECT rejected: missing Bearer token");
      return null;
    }

    String token = authHeader.substring(BEARER_PREFIX.length()).trim();

    try {
      UUID userId = jwtService.extractUserId(token);
      CustomUserDetails userDetails =
        (CustomUserDetails) customUserDetailsService.loadUserByUsername(userId.toString());

      if (!jwtService.isTokenValid(token, userDetails)) {
        log.warn("WebSocket CONNECT rejected: invalid token for user {}", userId);
        return null;
      }

      UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

      accessor.setUser(authentication);
      log.debug("WebSocket CONNECT authenticated: {}", userId);
      return message;
    } catch (Exception e) {
      log.warn("WebSocket CONNECT rejected: {}", e.getMessage());
      return null;
    }
  }

  private Message<?> handleProtectedFrame(Message<?> message, StompHeaderAccessor accessor) {
    CustomUserDetails userDetails = getUserDetails(accessor);

    if (userDetails == null) {
      log.warn("WebSocket {} rejected: unauthenticated", accessor.getCommand());
      return null;
    }

    String destination = accessor.getDestination();

    if (destination == null || destination.isBlank()) {
      return message;
    }

    UUID currentUserId = userDetails.getIdUser();

    if (destination.startsWith("/app/chat/send/")) {
      return allowIfCanAccess(message, destination, "/app/chat/send/", currentUserId, "chat SEND");
    }

    if (destination.startsWith("/topic/chat/")) {
      return allowIfCanAccess(message, destination, "/topic/chat/", currentUserId, "chat SUBSCRIBE");
    }

    if (destination.startsWith("/app/location/update/")) {
      return allowIfCanSendLocation(message, destination, "/app/location/update/", currentUserId);
    }

    if (destination.startsWith("/topic/location/")) {
      return allowIfCanAccess(message, destination, "/topic/location/", currentUserId, "location SUBSCRIBE");
    }

    return message;
  }

  private Message<?> allowIfCanAccess(
    Message<?> message,
    String destination,
    String prefix,
    UUID currentUserId,
    String action
  ) {
    UUID serviceRequestId = extractServiceRequestId(destination, prefix);

    if (serviceRequestId == null) {
      log.warn("WebSocket {} rejected: invalid destination {}", action, destination);
      return null;
    }

    boolean allowed = webSocketAuthorizationService.canAccessServiceRequest(serviceRequestId, currentUserId);

    if (!allowed) {
      log.warn(
        "WebSocket {} rejected: user {} cannot access serviceRequest {}",
        action,
        currentUserId,
        serviceRequestId
      );
      return null;
    }

    return message;
  }

  private Message<?> allowIfCanSendLocation(
    Message<?> message,
    String destination,
    String prefix,
    UUID currentUserId
  ) {
    UUID serviceRequestId = extractServiceRequestId(destination, prefix);

    if (serviceRequestId == null) {
      log.warn("WebSocket location SEND rejected: invalid destination {}", destination);
      return null;
    }

    boolean allowed = webSocketAuthorizationService.canSendLocation(serviceRequestId, currentUserId);

    if (!allowed) {
      log.warn(
        "WebSocket location SEND rejected: user {} is not assigned technician for serviceRequest {}",
        currentUserId,
        serviceRequestId
      );
      return null;
    }

    return message;
  }

  private String getAuthorizationHeader(StompHeaderAccessor accessor) {
    String authHeader = accessor.getFirstNativeHeader("Authorization");

    if (authHeader == null || authHeader.isBlank()) {
      authHeader = accessor.getFirstNativeHeader("authorization");
    }

    return authHeader == null ? null : authHeader.trim();
  }

  private CustomUserDetails getUserDetails(StompHeaderAccessor accessor) {
    Principal principal = accessor.getUser();

    if (principal instanceof UsernamePasswordAuthenticationToken authentication
      && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
      return userDetails;
    }

    return null;
  }

  private UUID extractServiceRequestId(String destination, String prefix) {
    try {
      String rawId = destination.substring(prefix.length()).trim();

      if (rawId.contains("/")) {
        rawId = rawId.substring(0, rawId.indexOf('/'));
      }

      return UUID.fromString(rawId);
    } catch (Exception e) {
      return null;
    }
  }
}

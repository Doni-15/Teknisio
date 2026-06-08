package com.teknisio.config;

import com.teknisio.security.CustomUserDetails;
import com.teknisio.security.CustomUserDetailsService;
import com.teknisio.security.JwtService;
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

import java.util.UUID;

/**
 * Intercepts STOMP CONNECT frames to authenticate the user
 * from the JWT token passed as a STOMP header.
 *
 * <p>The mobile client sends raw STOMP frames without SockJS,
 * so they bypass the HTTP security filter. This interceptor
 * fills the security gap by extracting the JWT from the
 * STOMP CONNECT header and setting the principal.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;
    private final CustomUserDetailsService customUserDetailsService;

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(
                message, StompHeaderAccessor.class);

        if (accessor == null) {
            return message;
        }

        // Only authenticate on CONNECT frames
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader(AUTH_HEADER);

            if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
                String token = authHeader.substring(BEARER_PREFIX.length());

                try {
                    UUID userId = jwtService.extractUserId(token);
                    CustomUserDetails userDetails = (CustomUserDetails) customUserDetailsService
                            .loadUserByUsername(userId.toString());

                    if (jwtService.isTokenValid(token, userDetails)) {
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails, null, userDetails.getAuthorities());

                        accessor.setUser(authentication);
                        log.debug("WebSocket STOMP user authenticated: {}", userId);
                    }

                } catch (Exception e) {
                    log.warn("Failed to authenticate WebSocket STOMP user: {}", e.getMessage());
                }
            } else {
                log.warn("WebSocket STOMP CONNECT without Authorization header");
            }
        }

        return message;
    }
}
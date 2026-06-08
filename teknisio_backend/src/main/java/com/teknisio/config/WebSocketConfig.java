package com.teknisio.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configures STOMP over WebSocket with a SockJS fallback.
 *
 * <p>Endpoint: {@code /ws} (raw WS) or {@code /ws/**} (SockJS)
 * <p>App destination prefix: {@code /app}  → routed to @MessageMapping handlers
 * <p>Broker prefix: {@code /topic}          → simple in-memory broker
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

  private final WebSocketAuthInterceptor webSocketAuthInterceptor;

  @Override
  public void configureMessageBroker(MessageBrokerRegistry config) {
    // Enable a simple in-memory broker for topics
    config.enableSimpleBroker("/topic");
    // Prefix for messages from clients to server-side handlers
    config.setApplicationDestinationPrefixes("/app");
  }

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    // Raw WebSocket endpoint for native clients (Android/iOS)
    registry
      .addEndpoint("/ws")
      .setAllowedOriginPatterns("*");

    // SockJS fallback endpoint for web browsers
    registry
      .addEndpoint("/ws-sockjs")
      .setAllowedOriginPatterns("*")
      .withSockJS();
  }

  @Override
  public void configureClientInboundChannel(ChannelRegistration registration) {
    registration.interceptors(webSocketAuthInterceptor);
  }
}

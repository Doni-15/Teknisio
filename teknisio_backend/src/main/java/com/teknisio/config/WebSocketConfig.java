package com.teknisio.config;

import org.springframework.context.annotation.Configuration;
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
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

  @Override
  public void configureMessageBroker(MessageBrokerRegistry config) {
    // Enable a simple in-memory broker for topics
    config.enableSimpleBroker("/topic");
    // Prefix for messages from clients to server-side handlers
    config.setApplicationDestinationPrefixes("/app");
  }

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry
      .addEndpoint("/ws")
      .setAllowedOriginPatterns("*")
      .withSockJS();
  }
}

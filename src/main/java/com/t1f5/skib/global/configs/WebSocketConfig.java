package com.t1f5.skib.global.configs;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker // ✅ 이것이 핵심
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

  @Override
  public void configureMessageBroker(MessageBrokerRegistry registry) {
    registry.enableSimpleBroker("/topic"); // 프론트가 구독할 주소
    registry.setApplicationDestinationPrefixes("/app"); // 메시지 전송 주소 prefix
  }

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry
        .addEndpoint("/ws") // 프론트가 연결할 WebSocket 엔드포인트
        .setAllowedOrigins("*") // CORS 허용
        .withSockJS(); // SockJS fallback 지원
  }
}

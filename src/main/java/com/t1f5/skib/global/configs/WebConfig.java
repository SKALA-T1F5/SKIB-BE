package com.t1f5.skib.global.configs;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry
        .addMapping("/**") // 모든 경로 허용
        .allowedOrigins("http://10.250.72.251:5173/**")
        .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH") // 허용할 HTTP 메서드
        .allowedHeaders("*") // 모든 헤더 허용
        .allowCredentials(true);
  }
}

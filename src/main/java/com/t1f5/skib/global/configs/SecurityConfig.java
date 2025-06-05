package com.t1f5.skib.global.configs;

import com.t1f5.skib.auth.jwt.JwtAuthenticationFilter;
import com.t1f5.skib.auth.service.CustomUserDetailsService;
import com.t1f5.skib.auth.util.JwtTokenProvider;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {

  private final JwtTokenProvider jwtTokenProvider;
  private final CustomUserDetailsService userDetailsService;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            authz ->
                authz
                    .requestMatchers(
                        "/api/auth/admin/login",
                        "/api/auth/user/login",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "api/admin")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .addFilterBefore(
            new JwtAuthenticationFilter(jwtTokenProvider, userDetailsService),
            UsernamePasswordAuthenticationFilter.class)
        .cors(cors -> cors.disable());

    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(); // 비밀번호 암호화용 Bean
  }

  // CORS 설정: Vue 개발 서버와 통신 허용
  @Bean
  public CorsFilter corsFilter() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowCredentials(true);
    config.setAllowedOriginPatterns(List.of("*")); // 또는 "http://localhost:5173"처럼 명확히 작성
    config.addAllowedHeader("*");
    config.addAllowedMethod("*");
    config.setExposedHeaders(List.of("Authorization")); // 클라이언트가 응답 헤더로 토큰 확인 가능

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return new CorsFilter(source);
  }
}
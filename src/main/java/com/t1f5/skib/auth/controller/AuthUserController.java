package com.t1f5.skib.auth.controller;

import com.t1f5.skib.auth.dto.requestdto.UserLoginRequestDto;
import com.t1f5.skib.auth.dto.responsedto.LoginResponseDto;
import com.t1f5.skib.auth.dto.responsedto.LogoutResponseDto;
import com.t1f5.skib.auth.service.AuthUserService;
import com.t1f5.skib.global.customAnnotations.SwaggerApiNotFoundError;
import com.t1f5.skib.global.customAnnotations.SwaggerApiSuccess;
import com.t1f5.skib.global.customAnnotations.SwaggerInternetServerError;
import com.t1f5.skib.global.dtos.ResultDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/user")
@RequiredArgsConstructor
@Tag(name = "User Auth API", description = "유저 인증 관련 API")
public class AuthUserController {

  private final AuthUserService authUserService;

  @SwaggerApiSuccess(
      summary = "유저 로그인",
      description = "이메일과 비밀번호를 JSON 형태로 Body에 담아 요청하면 JWT 토큰이 발급됩니다.")
  @SwaggerApiNotFoundError
  @SwaggerInternetServerError
  @PostMapping("/login")
  public ResponseEntity<?> login(@RequestBody UserLoginRequestDto request) {
    LoginResponseDto result = authUserService.login(request);
    return ResponseEntity.ok(ResultDto.res(HttpStatus.OK, "SUCCESS", result));
  }

  @SwaggerApiSuccess(
      summary = "유저 로그아웃",
      description = "Authorization 헤더에 JWT 토큰을 담아 요청하면 로그아웃 처리됩니다.")
  @SwaggerApiNotFoundError
  @SwaggerInternetServerError
  @PostMapping("/logout")
  public ResponseEntity<?> logout(@RequestHeader("Authorization") String token) {
    LogoutResponseDto result = authUserService.logout(token);
    return ResponseEntity.ok(ResultDto.res(HttpStatus.OK, "SUCCESS", result));
  }
}

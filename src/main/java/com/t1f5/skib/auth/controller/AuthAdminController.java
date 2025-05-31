package com.t1f5.skib.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.t1f5.skib.auth.dto.requestdto.AdminLoginRequestDto;

import com.t1f5.skib.auth.dto.responsedto.LoginResponseDto;
import com.t1f5.skib.auth.dto.responsedto.LogoutResponseDto;
import com.t1f5.skib.auth.service.AuthAdminService;

import com.t1f5.skib.global.customAnnotations.SwaggerApiNotFoundError;
import com.t1f5.skib.global.customAnnotations.SwaggerApiSuccess;
import com.t1f5.skib.global.customAnnotations.SwaggerInternetServerError;
import com.t1f5.skib.global.dtos.ResultDto;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth/admin")
@RequiredArgsConstructor
@Tag(name = "Admin Auth API", description = "관리자 인증 관련 API")
public class AuthAdminController {

    private final AuthAdminService authAdminService;

    @SwaggerApiSuccess(summary = "관리자 로그인", description = "관리자 ID와 비밀번호를 Body에 담아 요청하면 JWT 토큰이 발급됩니다.")
    @SwaggerApiNotFoundError
    @SwaggerInternetServerError
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AdminLoginRequestDto request) {
        LoginResponseDto result = authAdminService.login(request);
        return ResponseEntity.ok(ResultDto.res(HttpStatus.OK, "SUCCESS", result));
    }

    @SwaggerApiSuccess(summary = "관리자 로그아웃", description = "Authorization 헤더에 JWT 토큰을 담아 요청하면 로그아웃 처리됩니다.")
    @SwaggerApiNotFoundError
    @SwaggerInternetServerError
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String token) {
        LogoutResponseDto result = authAdminService.logout(token);
        return ResponseEntity.ok(ResultDto.res(HttpStatus.OK, "SUCCESS", result));
    }
}

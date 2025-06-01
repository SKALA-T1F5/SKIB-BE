package com.t1f5.skib.admin.controller;

import com.t1f5.skib.admin.dto.RequestCreateAdminDto;
import com.t1f5.skib.admin.service.AdminService;
import com.t1f5.skib.global.customAnnotations.SwaggerApiNotFoundError;
import com.t1f5.skib.global.customAnnotations.SwaggerApiSuccess;
import com.t1f5.skib.global.customAnnotations.SwaggerInternetServerError;
import com.t1f5.skib.global.dtos.ResultDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin API", description = "관리자 관련 API")
@RequiredArgsConstructor
public class AdminController {
  private final AdminService adminService;

  @SwaggerApiSuccess(summary = "관리자 계정 생성", description = "새로운 관리자 계정을 생성합니다.")
  @SwaggerApiNotFoundError
  @SwaggerInternetServerError
  @PostMapping
  public ResponseEntity<?> createAdmin(@Valid @RequestBody RequestCreateAdminDto dto) {
    adminService.createAdmin(dto);
    return ResponseEntity.ok(ResultDto.res(HttpStatus.OK, "SUCCESS", "관리자 계정 생성 완료"));
  }
}

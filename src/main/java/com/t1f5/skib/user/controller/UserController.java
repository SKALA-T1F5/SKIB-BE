package com.t1f5.skib.user.controller;

import com.t1f5.skib.global.customAnnotations.SwaggerApiNotFoundError;
import com.t1f5.skib.global.customAnnotations.SwaggerApiSuccess;
import com.t1f5.skib.global.customAnnotations.SwaggerInternetServerError;
import com.t1f5.skib.global.dtos.ResultDto;
import com.t1f5.skib.user.dto.requestdto.RequestCreateUserDto;
import com.t1f5.skib.user.dto.requestdto.RequestUpdateUserDto;
import com.t1f5.skib.user.service.UserService;

import org.springframework.http.HttpStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/user")
@Tag(name = "User API", description = "유저 관련 API")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    // 1. 유저 생성 (여러 명을 한꺼번에)
    @SwaggerApiSuccess(summary = "유저 생성", description = "새로운 유저를 생성합니다.")
    @SwaggerApiNotFoundError
    @SwaggerInternetServerError
    @PostMapping
    public ResponseEntity<?> createUsers(@Valid @RequestBody RequestCreateUserDto dto) {
        userService.createUsers(dto);
        return ResponseEntity.ok(ResultDto.res(HttpStatus.OK, "SUCCESS", "유저 생성 완료"));
    }

    // 2. 유저 정보 수정
    @SwaggerApiSuccess(summary = "유저 정보 수정", description = "특정 유저의 정보를 수정합니다.")
    @SwaggerApiNotFoundError
    @SwaggerInternetServerError
    @PutMapping("/update")
    public ResponseEntity<?> updateUser(@Valid @RequestBody RequestUpdateUserDto dto, Integer userId) {
        userService.updateUser(userId, dto);
        return ResponseEntity.ok(ResultDto.res(HttpStatus.OK, "SUCCESS", "유저 정보 수정 완료"));
    }

    // 3. 유저 삭제
    @SwaggerApiSuccess(summary = "유저 삭제", description = "특정 유저를 삭제합니다.")
    @SwaggerApiNotFoundError
    @SwaggerInternetServerError
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteUser(@Valid @RequestBody Integer userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok(ResultDto.res(HttpStatus.OK, "SUCCESS", "유저 삭제 완료"));
    }
}

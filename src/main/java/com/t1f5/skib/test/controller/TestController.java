package com.t1f5.skib.test.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

import com.t1f5.skib.global.customAnnotations.SwaggerApiNotFoundError;
import com.t1f5.skib.global.customAnnotations.SwaggerApiSuccess;
import com.t1f5.skib.global.customAnnotations.SwaggerInternetServerError;
import com.t1f5.skib.global.dtos.ResultDto;
import com.t1f5.skib.test.dto.RequestCreateTestDto;
import com.t1f5.skib.test.service.TestService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/test")
@Tag(name = "Test API", description = "테스트 관련 API")
@RequiredArgsConstructor
public class TestController {
    private final TestService testService;

    @SwaggerApiSuccess(summary = "테스트 생성", description = "새로운 테스트를 생성합니다.")
    @SwaggerApiNotFoundError
    @SwaggerInternetServerError
    @PostMapping
    public ResponseEntity<?> saveTest(@RequestBody RequestCreateTestDto requestCreateTestDto, Integer projectId) {
        testService.saveTest(projectId, requestCreateTestDto);

        return ResponseEntity.ok(ResultDto.res(HttpStatus.OK, "SUCCESS", "테스트가 성공적으로 생성되었습니다."));
    }
}

package com.t1f5.skib.test.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.t1f5.skib.global.customAnnotations.SwaggerApiNotFoundError;
import com.t1f5.skib.global.customAnnotations.SwaggerApiSuccess;
import com.t1f5.skib.global.customAnnotations.SwaggerInternetServerError;
import com.t1f5.skib.global.dtos.ResultDto;
import com.t1f5.skib.test.dto.RequestCreateTestDto;
import com.t1f5.skib.test.dto.ResponseTestDto;
import com.t1f5.skib.test.dto.ResponseTestListDto;
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
    public ResponseEntity<ResultDto<String>> saveTest(@RequestBody RequestCreateTestDto requestCreateTestDto, Integer projectId) {
        String inviteLink = testService.saveTest(projectId, requestCreateTestDto);

        return ResponseEntity.ok(ResultDto.res(HttpStatus.OK, "SUCCESS", inviteLink));
    }

    @SwaggerApiSuccess(summary = "테스트 단일 조회", description = "테스트 ID로 테스트를 조회합니다.")
    @SwaggerApiNotFoundError
    @SwaggerInternetServerError
    @GetMapping("/getTest")
    public ResponseEntity<ResultDto<ResponseTestDto>> getTestById(@RequestParam Integer testId) {
        ResponseTestDto result = testService.getTestById(testId);
        return ResponseEntity.ok(ResultDto.res(HttpStatus.OK, "SUCCESS", result));
    }

    @SwaggerApiSuccess(summary = "테스트 전체 조회", description = "특정 프로젝트의 모든 테스트 목록을 조회합니다.")
    @SwaggerApiNotFoundError
    @SwaggerInternetServerError
    @GetMapping("/getTests")
    public ResponseEntity<ResultDto<ResponseTestListDto>> getAllTests(@RequestParam Integer projectId) {
        ResponseTestListDto result = testService.getAllTests(projectId);
        return ResponseEntity.ok(ResultDto.res(HttpStatus.OK, "SUCCESS", result));
    }

    @SwaggerApiSuccess(summary = "테스트 삭제", description = "테스트 ID로 테스트를 삭제합니다.")
    @SwaggerApiNotFoundError
    @SwaggerInternetServerError
    @DeleteMapping("/deleteTest")
    public ResponseEntity<ResultDto<String>> deleteTest(@RequestParam Integer testId) {
        testService.deleteTest(testId);
        return ResponseEntity.ok(ResultDto.res(HttpStatus.OK, "SUCCESS", "테스트가 성공적으로 삭제되었습니다."));
    }
}

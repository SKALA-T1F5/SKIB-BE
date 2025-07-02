package com.t1f5.skib.question.controller;

import com.t1f5.skib.global.customAnnotations.SwaggerApiSuccess;
import com.t1f5.skib.global.dtos.ResultDto;
import com.t1f5.skib.question.dto.QuestionValueDto;
import com.t1f5.skib.question.dto.TestResultDto;
import com.t1f5.skib.question.service.QuestionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/questions")
@Tag(name = "Question API", description = "문제 관련 API")
@RequiredArgsConstructor
public class QuestionController {

  private final QuestionService questionService;

  @SwaggerApiSuccess(summary = "문제 수정", description = "MongoDB에서 특정 키에 해당하는 문제를 수정합니다.")
  @PutMapping("/update")
  public ResponseEntity<ResultDto<Void>> updateQuestion(
      @RequestParam String key, @RequestBody QuestionValueDto updatedValueDto) {
    questionService.updateQuestion(key, updatedValueDto);
    return ResponseEntity.ok(ResultDto.res(HttpStatus.OK, "SUCCESS", null));
  }

  @SwaggerApiSuccess(summary="문제 저장", description="MongoDB에 문제를 저장합니다.")
  @PutMapping("/result")
  public ResponseEntity<ResultDto<Void>> receiveTestResult(@RequestBody TestResultDto resultDto) {
    questionService.saveGeneratedQuestions(resultDto);
    return ResponseEntity.ok(ResultDto.res(HttpStatus.OK, "SUCCESS", null));
  }
}

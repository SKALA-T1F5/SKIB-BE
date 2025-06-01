package com.t1f5.skib.question.controller;

import com.t1f5.skib.global.customAnnotations.SwaggerApiNotFoundError;
import com.t1f5.skib.global.customAnnotations.SwaggerApiSuccess;
import com.t1f5.skib.global.customAnnotations.SwaggerInternetServerError;
import com.t1f5.skib.global.dtos.ResultDto;
import com.t1f5.skib.question.dto.RequestCreateQuestionDto;
import com.t1f5.skib.question.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
public class QuestionController {

  private final QuestionService questionService;

  @SwaggerApiSuccess(
      summary = "문제 생성 및 저장",
      description = "FastAPI를 통해 각 문서별 문제를 생성하고 MongoDB에 저장합니다.")
  @SwaggerApiNotFoundError
  @SwaggerInternetServerError
  @PostMapping
  public ResponseEntity<ResultDto<?>> generateQuestions(
      @RequestBody RequestCreateQuestionDto request) {
    questionService.generateQuestions(request.getDocuments());
    return ResponseEntity.ok(ResultDto.res(HttpStatus.OK, "문제 생성 및 저장 완료"));
  }
}

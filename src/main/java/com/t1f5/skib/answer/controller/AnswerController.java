package com.t1f5.skib.answer.controller;

import com.t1f5.skib.answer.dto.RequestCreateAnswerDto;
import com.t1f5.skib.answer.service.AnswerService;
import com.t1f5.skib.global.customAnnotations.SwaggerApiNotFoundError;
import com.t1f5.skib.global.customAnnotations.SwaggerApiSuccess;
import com.t1f5.skib.global.customAnnotations.SwaggerInternetServerError;
import com.t1f5.skib.global.dtos.ResultDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/answer")
public class AnswerController {

  private final AnswerService answerService;

  @SwaggerApiSuccess(
      summary = "답변 제출",
      description = "사용자가 테스트에 대한 답변을 제출합니다. 각 답변은 사용자 테스트 ID와 함께 전송됩니다.")
  @SwaggerApiNotFoundError
  @SwaggerInternetServerError
  @PostMapping
  public ResponseEntity<ResultDto<?>> submitAnswers(
      Integer userId, Integer testId, @RequestBody RequestCreateAnswerDto dto) {

    answerService.saveAnswer(dto, userId, testId);
    return ResponseEntity.ok(ResultDto.res(HttpStatus.OK, "답변이 성공적으로 저장되었습니다."));
  }

  @SwaggerApiSuccess(summary = "채점 결과 조회", description = "사용자의 채점결과를 반환합니다.")
  @SwaggerApiNotFoundError
  @SwaggerInternetServerError
  @GetMapping("/getResult")
  public ResponseEntity<ResultDto<?>> getScoredAnswersByUserTestId(
      Integer userId, Integer testId, String lang) {
    answerService.getScoredAnswersByUserTestId(userId, testId, lang);
    return ResponseEntity.ok(ResultDto.res(HttpStatus.OK, "답변을 성공적으로 조회했습니다."));
  }
}

package com.t1f5.skib.feedback.controller;

import com.t1f5.skib.feedback.dto.ResponseFeedbackAllDto;
import com.t1f5.skib.feedback.dto.ResponseFeedbackDocDto;
import com.t1f5.skib.feedback.dto.ResponseFeedbackTagDto;
import com.t1f5.skib.feedback.service.FeedbackService;
import com.t1f5.skib.global.dtos.ResultDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/feedback")
@Tag(name = "Feedback API", description = "피드백 관련 API")
@RequiredArgsConstructor
public class FeedbackController {

  private final FeedbackService feedbackService;

  @GetMapping("/all")
  public ResponseEntity<ResultDto<ResponseFeedbackAllDto>> getAllFeedback(
          @RequestParam Integer userId,
          @RequestParam Integer testId) {

      ResponseFeedbackAllDto response = feedbackService.getTotalAccuracyRate(userId, testId);

      return ResponseEntity.ok(ResultDto.res(HttpStatus.OK, "총 정답률 조회 성공", response));
  }

  @GetMapping("/docs")
  public ResponseEntity<ResultDto<List<ResponseFeedbackDocDto>>> getDocumentFeedback(
          @RequestParam Integer userId,
          @RequestParam Integer testId) {

      List<ResponseFeedbackDocDto> response = feedbackService.getDocumentAccuracyRates(userId, testId);

      return ResponseEntity.ok(ResultDto.res(HttpStatus.OK, "문서별 정답률 조회 성공", response));
  }

  @GetMapping("/tag")
  public ResponseEntity<ResultDto<List<ResponseFeedbackTagDto>>> getTagFeedback(
          @RequestParam Integer userId,
          @RequestParam Integer testId) {

      List<ResponseFeedbackTagDto> response = feedbackService.getTagAccuracyRates(userId, testId);

      return ResponseEntity.ok(ResultDto.res(HttpStatus.OK, "태그별 정답률 조회 성공", response));
  }
}
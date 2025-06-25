package com.t1f5.skib.feedback.controller;

import com.t1f5.skib.feedback.dto.ResponseAnswerMatrixDto;
import com.t1f5.skib.feedback.dto.ResponseFeedbackAllDto;
import com.t1f5.skib.feedback.dto.ResponseFeedbackDistributionDto;
import com.t1f5.skib.feedback.dto.ResponseFeedbackDocDto;
import com.t1f5.skib.feedback.dto.ResponseFeedbackTagDto;
import com.t1f5.skib.feedback.dto.ResponseTestTagDto;
import com.t1f5.skib.feedback.dto.ResponseTrainerTestStatisticsDto;
import com.t1f5.skib.feedback.dto.TrainerFeedBackDto;
import com.t1f5.skib.feedback.service.FeedbackService;
import com.t1f5.skib.global.customAnnotations.SwaggerApiNotFoundError;
import com.t1f5.skib.global.customAnnotations.SwaggerApiSuccess;
import com.t1f5.skib.global.customAnnotations.SwaggerInternetServerError;
import com.t1f5.skib.global.dtos.ResultDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/feedback")
@Tag(name = "Feedback API", description = "피드백 관련 API")
@RequiredArgsConstructor
public class FeedbackController {

  private final FeedbackService feedbackService;

  @SwaggerApiSuccess(
      summary = "학습자별 테스트 결과 요약 조회",
      description = "사용자의 테스트 결과 요약을 조회 합니다(총 점수, 합격 점수, 정/오답 수. 사용자 ID와 테스트 ID를 요청 파라미터로 받습니다.")
  @SwaggerApiNotFoundError
  @SwaggerInternetServerError
  @GetMapping("/all")
  public ResponseEntity<ResultDto<ResponseFeedbackAllDto>> getAllFeedback(
      @RequestParam(value = "userId") Integer userId,
      @RequestParam(value = "testId") Integer testId) {

    ResponseFeedbackAllDto response = feedbackService.getFeedbackSummary(userId, testId);

    return ResponseEntity.ok(ResultDto.res(HttpStatus.OK, "테스트 결과 요약 조회 성공", response));
  }

  @SwaggerApiSuccess(
      summary = "학습자별 문서별 정답률 조회",
      description = "사용자의 테스트에 대한 문서별 정답률을 조회합니다. 사용자 ID와 테스트 ID를 요청 파라미터로 받습니다.")
  @SwaggerApiNotFoundError
  @SwaggerInternetServerError
  @GetMapping("/docs")
  public ResponseEntity<ResultDto<List<ResponseFeedbackDocDto>>> getDocumentFeedback(
      @RequestParam(value = "userId") Integer userId,
      @RequestParam(value = "testId") Integer testId) {

    List<ResponseFeedbackDocDto> response =
        feedbackService.getDocumentAccuracyRates(userId, testId);

    return ResponseEntity.ok(ResultDto.res(HttpStatus.OK, "문서별 정답률 조회 성공", response));
  }

  @SwaggerApiSuccess(
      summary = "학습자별 태그별 정답률 조회",
      description = "사용자의 테스트에 대한 태그별 정답률을 조회합니다. 사용자 ID와 테스트 ID를 요청 파라미터로 받습니다.")
  @SwaggerApiNotFoundError
  @SwaggerInternetServerError
  @GetMapping("/tag")
  public ResponseEntity<ResultDto<List<ResponseFeedbackTagDto>>> getTagFeedback(
      @RequestParam Integer userId, @RequestParam Integer testId) {

    List<ResponseFeedbackTagDto> response = feedbackService.getTagAccuracyRates(userId, testId);

    return ResponseEntity.ok(ResultDto.res(HttpStatus.OK, "태그별 정답률 조회 성공", response));
  }

  @SwaggerApiSuccess(
      summary = "학습자별 위치 조회 (+점수 분포)",
      description = "사용자의 테스트에 대한 점수 분포를 조회합니다. 사용자 ID와 테스트 ID를 요청 파라미터로 받습니다.")
  @SwaggerApiNotFoundError
  @SwaggerInternetServerError
  @GetMapping("/distribution")
  public ResponseEntity<ResultDto<ResponseFeedbackDistributionDto>> getScoreDistribution(
      @RequestParam Integer userId, @RequestParam Integer testId) {

    ResponseFeedbackDistributionDto response = feedbackService.getScoreDistribution(userId, testId);
    return ResponseEntity.ok(ResultDto.res(HttpStatus.OK, "점수 분포 조회 성공", response));
  }

  @SwaggerApiSuccess(
      summary = "트레이너 피드백 조회",
      description = "트레이너가 제공하는 피드백을 조회합니다. 테스트 ID를 요청 파라미터로 받습니다.")
  @SwaggerApiNotFoundError
  @SwaggerInternetServerError
  @GetMapping("/trainer-feedback/top")
  public ResponseEntity<ResultDto<List<TrainerFeedBackDto>>> getTopQuestions(
      @RequestParam Integer testId) {
    List<TrainerFeedBackDto> response =
        feedbackService.getQuestionFeedbackSortedByTestId(testId, true);
    return ResponseEntity.ok(ResultDto.res(HttpStatus.OK, "문항 정답률 상위 정렬 반환 성공", response));
  }

  @SwaggerApiSuccess(
      summary = "문항 정답률 하위 정렬 조회",
      description = "문항 정답률 하위 정렬을 조회합니다. 테스트 ID를 요청 파라미터로 받습니다.")
  @SwaggerApiNotFoundError
  @SwaggerInternetServerError
  @GetMapping("/trainer-feedback/bottom")
  public ResponseEntity<ResultDto<List<TrainerFeedBackDto>>> getBottomQuestions(
      @RequestParam Integer testId) {
    List<TrainerFeedBackDto> response =
        feedbackService.getQuestionFeedbackSortedByTestId(testId, false);
    return ResponseEntity.ok(ResultDto.res(HttpStatus.OK, "문항 정답률 하위 정렬 반환 성공", response));
  }

  @SwaggerApiSuccess(
      summary = "테스트별 기본 통계(합격자 수, 평균점수 등) 조회",
      description = "특정 테스트에 대한 평균 점수, 응시자 수, 합격자 수, 패스 점수를 조회합니다.")
  @SwaggerApiNotFoundError
  @SwaggerInternetServerError
  @GetMapping("/test-basic-statistics")
  public ResponseEntity<ResultDto<ResponseTrainerTestStatisticsDto>> getTestBasicStatistics(
      @RequestParam Integer testId) {

    ResponseTrainerTestStatisticsDto response = feedbackService.getTestBasicStatistics(testId);
    return ResponseEntity.ok(ResultDto.res(HttpStatus.OK, "테스트 기본 통계 조회 성공", response));
  }

  @SwaggerApiSuccess(
      summary = "트레이너 피드백 생성",
      description = "트레이너가 제공하는 피드백을 생성합니다. 요청 본문에 피드백 데이터를 포함합니다.")
  @SwaggerApiNotFoundError
  @SwaggerInternetServerError
  @PostMapping("/trainer-feedback")
  public ResponseEntity<ResultDto<String>> generateFeedbackForTest(@RequestParam Integer testId) {
    String response = feedbackService.generateFeedbackForTest(testId);
    return ResponseEntity.ok(ResultDto.res(HttpStatus.OK, "트레이너 피드백 생성 성공", response));
  }

  @SwaggerApiSuccess(
      summary = "테스트별 학습자별 문제별 정답 현황(정오표) 조회",
      description = "특정 테스트에 대해 학습자별 문제별 정답 여부를 조회합니다.")
  @SwaggerApiNotFoundError
  @SwaggerInternetServerError
  @GetMapping("/answer-matrix")
  public ResponseEntity<ResultDto<ResponseAnswerMatrixDto>> getAnswerMatrix(
      @RequestParam Integer testId) {

    ResponseAnswerMatrixDto response = feedbackService.getAnswerMatrix(testId);
    return ResponseEntity.ok(ResultDto.res(HttpStatus.OK, "정오표 조회 성공", response));
  }

  @SwaggerApiSuccess(
      summary = "테스트별 태그별 정답률 조회",
      description = "테스트에 포함된 문제들의 태그별 평균 정답률을 계산하여 반환합니다.")
  @SwaggerApiNotFoundError
  @SwaggerInternetServerError
  @GetMapping("/tag-by-test")
  public ResponseEntity<ResultDto<List<ResponseTestTagDto>>> getTagAccuracyByTest(
      @RequestParam Integer testId) {

    List<ResponseTestTagDto> response = feedbackService.getTagAccuracyRatesByTestId(testId);
    return ResponseEntity.ok(ResultDto.res(HttpStatus.OK, "태그별 정답률 조회 성공", response));
  }
}
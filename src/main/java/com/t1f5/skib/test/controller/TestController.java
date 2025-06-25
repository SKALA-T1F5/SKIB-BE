package com.t1f5.skib.test.controller;

import com.t1f5.skib.global.customAnnotations.SwaggerApiNotFoundError;
import com.t1f5.skib.global.customAnnotations.SwaggerApiSuccess;
import com.t1f5.skib.global.customAnnotations.SwaggerInternetServerError;
import com.t1f5.skib.global.dtos.ResultDto;
import com.t1f5.skib.question.domain.Question;
import com.t1f5.skib.test.dto.DocumentQuestionCountDto;
import com.t1f5.skib.test.dto.RequestCreateTestDto;
import com.t1f5.skib.test.dto.RequestFinalizeTestDto;
import com.t1f5.skib.test.dto.RequestSaveRandomTestDto;
import com.t1f5.skib.test.dto.ResponseTestDto;
import com.t1f5.skib.test.dto.ResponseTestInitDto;
import com.t1f5.skib.test.dto.ResponseTestListDto;
import com.t1f5.skib.test.dto.ResponseTestSummaryListDto;
import com.t1f5.skib.test.service.TestService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
@Tag(name = "Test API", description = "테스트 관련 API")
@RequiredArgsConstructor
public class TestController {
  private final TestService testService;

  @SwaggerApiSuccess(summary = "LLM을 통한 테스트 생성", description = "LLM을 사용하여 새로운 테스트를 생성합니다.")
  @SwaggerApiNotFoundError
  @SwaggerInternetServerError
  @PostMapping("/createByLLM")
  public ResponseEntity<ResultDto<String>> makeTest(
      @RequestParam(value = "userInput") String userInput,
      @RequestParam(value = "projectId") Integer projectId) {
    String response = testService.makeTest(projectId, userInput);

    return ResponseEntity.ok(ResultDto.res(HttpStatus.OK, "SUCCESS", response));
  }

  @SwaggerApiSuccess(summary = "테스트 생성", description = "새로운 테스트를 생성합니다.")
  @SwaggerApiNotFoundError
  @SwaggerInternetServerError
  @PostMapping
  public ResponseEntity<ResultDto<?>> saveTest(
      @RequestParam("projectId") Integer projectId, @RequestBody RequestCreateTestDto requestDto) {
    ResponseTestInitDto response = testService.saveTestWithQuestions(projectId, requestDto);
    return ResponseEntity.ok(ResultDto.res(HttpStatus.OK, "SUCCESS", response));
  }

  @SwaggerApiSuccess(summary = "테스트 최종 확정", description = "테스트를 최종 확정합니다.")
  @SwaggerApiNotFoundError
  @SwaggerInternetServerError
  @PostMapping("/finalize")
  public ResponseEntity<ResultDto<?>> finalizeTest(
      @RequestBody RequestFinalizeTestDto requestDto, @RequestParam("testId") Integer testId) {
    testService.finalizeTest(testId, requestDto);
    return ResponseEntity.ok(ResultDto.res(HttpStatus.OK, "SUCCESS", "테스트가 최종 확정되었습니다."));
  }

  @SwaggerApiSuccess(summary = "랜덤 테스트 생성", description = "특정 프로젝트에서 랜덤으로 문제를 선택하여 테스트를 생성합니다.")
  @SwaggerApiNotFoundError
  @SwaggerInternetServerError
  @GetMapping("/random")
  public ResponseEntity<ResultDto<List<Question>>> generateRandomTest(
      @RequestParam("projectId") Integer projectId, @RequestParam("count") Integer count) {
    List<Question> questions = testService.generateRandomTest(projectId, count);
    return ResponseEntity.ok(ResultDto.res(HttpStatus.OK, "SUCCESS", questions));
  }

  @SwaggerApiSuccess(summary = "랜덤 테스트 저장", description = "랜덤으로 생성된 테스트를 저장합니다.")
  @SwaggerApiNotFoundError
  @SwaggerInternetServerError
  @PostMapping("/random/save")
  public ResponseEntity<ResultDto<Void>> saveRandomTest(@RequestBody RequestSaveRandomTestDto dto) {
    testService.saveRandomTest(dto);
    return ResponseEntity.ok(ResultDto.res(HttpStatus.OK, "SUCCESS", null));
  }

  @SwaggerApiSuccess(summary = "문서별 문제 수 조회", description = "특정 프로젝트의 문서별 문제 수를 조회합니다.")
  @SwaggerApiNotFoundError
  @SwaggerInternetServerError
  @GetMapping("/document-question-counts")
  public ResponseEntity<ResultDto<List<DocumentQuestionCountDto>>> getDocumentQuestionCounts(
      @RequestParam("projectId") Integer projectId) {
    List<DocumentQuestionCountDto> counts =
        testService.getDocumentQuestionCountsByProject(projectId);
    return ResponseEntity.ok(ResultDto.res(HttpStatus.OK, "SUCCESS", counts));
  }

  @SwaggerApiSuccess(summary = "테스트 단일 조회", description = "테스트 ID로 테스트를 조회합니다.")
  @SwaggerApiNotFoundError
  @SwaggerInternetServerError
  @GetMapping("/getTest")
  public ResponseEntity<ResultDto<ResponseTestDto>> getTestById(
      @RequestParam(value = "testId") Integer testId,
      @RequestParam(defaultValue = "ko") String lang) {
    ResponseTestDto result = testService.getTestById(testId, lang);
    return ResponseEntity.ok(ResultDto.res(HttpStatus.OK, "SUCCESS", result));
  }

  @SwaggerApiSuccess(summary = "유저 테스트 단일 조회", description = "유저 테스트 ID로 테스트를 조회합니다.")
  @SwaggerApiNotFoundError
  @SwaggerInternetServerError
  @GetMapping("/getUserTest")
  public ResponseEntity<ResultDto<ResponseTestDto>> getTestByUserTestId(
      @RequestParam Integer userId, @RequestParam Integer testId) {
    ResponseTestDto result = testService.getTestByUserTestId(userId, testId);
    return ResponseEntity.ok(ResultDto.res(HttpStatus.OK, "SUCCESS", result));
  }

  @SwaggerApiSuccess(summary = "유저 테스트 목록 조회", description = "유저 ID로 유저의 테스트 목록을 조회합니다.")
  @SwaggerApiNotFoundError
  @SwaggerInternetServerError
  @GetMapping("/getUserTestList")
  public ResponseEntity<ResultDto<ResponseTestSummaryListDto>> getUserTestList(
      @RequestParam Integer userId) {
    ResponseTestSummaryListDto result = testService.getUserTestList(userId);
    return ResponseEntity.ok(ResultDto.res(HttpStatus.OK, "SUCCESS", result));
  }

  @SwaggerApiSuccess(summary = "테스트 전체 조회", description = "특정 프로젝트의 모든 테스트 목록을 조회합니다.")
  @SwaggerApiNotFoundError
  @SwaggerInternetServerError
  @GetMapping("/getTests")
  public ResponseEntity<ResultDto<ResponseTestListDto>> getAllTests(
      @RequestParam Integer projectId) {
    ResponseTestListDto result = testService.getAllTests(projectId);
    return ResponseEntity.ok(ResultDto.res(HttpStatus.OK, "SUCCESS", result));
  }

  @SwaggerApiSuccess(summary = "테스트 초대링크 조회", description = "테스트 ID로 초대링크를 조회합니다.")
  @SwaggerApiNotFoundError
  @SwaggerInternetServerError
  @GetMapping("/getInviteLink")
  public ResponseEntity<ResultDto<String>> getInviteLink(@RequestParam Integer testId) {
    String inviteLink = testService.getInviteLink(testId);
    return ResponseEntity.ok(ResultDto.res(HttpStatus.OK, "SUCCESS", inviteLink));
  }

  @SwaggerApiSuccess(summary = "테스트 삭제", description = "테스트 ID로 테스트를 삭제합니다.")
  @SwaggerApiNotFoundError
  @SwaggerInternetServerError
  @DeleteMapping("/deleteTest")
  public ResponseEntity<ResultDto<String>> deleteTest(@RequestParam Integer testId) {
    testService.deleteTest(testId);
    return ResponseEntity.ok(ResultDto.res(HttpStatus.OK, "SUCCESS", "테스트가 성공적으로 삭제되었습니다."));
  }

  @SwaggerApiSuccess(summary = "초대링크로 유저 등록", description = "초대링크와 이메일을 기반으로 유저를 유저테스트에 등록합니다.")
  @SwaggerApiNotFoundError
  @SwaggerInternetServerError
  @PostMapping("/invite/register")
  public ResponseEntity<ResultDto<ResponseTestDto>> registerUserToTest(
      @RequestParam String token, Integer userId, @RequestParam(defaultValue = "ko") String lang) {

    ResponseTestDto response = testService.registerUserToTestAndReturnTest(token, userId, lang);
    return ResponseEntity.ok(ResultDto.res(HttpStatus.OK, "SUCCESS", response));
  }
}

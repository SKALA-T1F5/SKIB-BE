package com.t1f5.skib.test.service;

import com.t1f5.skib.global.dtos.DtoConverter;
import com.t1f5.skib.project.repository.ProjectJpaRepository;
import com.t1f5.skib.question.domain.Question;
import com.t1f5.skib.question.dto.QuestionDto;
import com.t1f5.skib.question.dto.RequestCreateQuestionDto;
import com.t1f5.skib.question.dto.ResponseQuestionDtoConverter;
import com.t1f5.skib.question.repository.QuestionMongoRepository;
import com.t1f5.skib.question.service.QuestionService;
import com.t1f5.skib.test.domain.InviteLink;
import com.t1f5.skib.test.domain.Test;
import com.t1f5.skib.test.domain.TestQuestion;
import com.t1f5.skib.test.domain.UserTest;
import com.t1f5.skib.test.dto.QuestionTranslator;
import com.t1f5.skib.test.dto.RequestCreateTestByLLMDto;
import com.t1f5.skib.test.dto.RequestCreateTestDto;
import com.t1f5.skib.test.dto.ResponseTestDto;
import com.t1f5.skib.test.dto.ResponseTestListDto;
import com.t1f5.skib.test.dto.TestDocumentConfigDto;
import com.t1f5.skib.test.dto.TestDtoConverter;
import com.t1f5.skib.test.repository.InviteLinkRepository;
import com.t1f5.skib.test.repository.TestQuestionRepository;
import com.t1f5.skib.test.repository.TestRepository;
import com.t1f5.skib.test.repository.UserTestRepository;
import com.t1f5.skib.user.model.User;
import com.t1f5.skib.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@RequiredArgsConstructor
@Slf4j
@Service
public class TestService {
  private final TestRepository testRepository;
  private final ProjectJpaRepository projectRepository;
  private final InviteLinkRepository inviteLinkRepository;
  private final UserRepository userRepository;
  private final UserTestRepository userTestRepository;
  private final QuestionMongoRepository questionMongoRepository;
  private final TestQuestionRepository testQuestionRepository;
  private final TestDtoConverter testDtoConverter;
  private final ResponseQuestionDtoConverter questionDtoConverter;
  private final WebClient webClient;
  private final QuestionService questionService;
  @Autowired private QuestionTranslator questionTranslator;

  /**
   * LLM을 사용하여 테스트를 생성합니다.
   *
   * @param projectId 프로젝트 ID
   * @param dto LLM 요청 DTO
   * @return 생성된 테스트의 응답
   */
  public String makeTest(Integer projectId, RequestCreateTestByLLMDto dto) {
    log.info("Creating test by LLM for project ID: {}", projectId);

    if (!projectRepository.existsById(projectId)) {
      throw new IllegalArgumentException("해당 프로젝트가 존재하지 않습니다: " + projectId);
    }

    String response =
        webClient
            .post()
            .uri("http://fastapi-service:8000/api/test/generate")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(dto)
            .retrieve()
            .bodyToMono(String.class)
            .block();

    log.info("FastAPI 응답: {}", response);

    return response;
  }

  /**
   * 테스트를 저장하고 초대 링크를 생성합니다.
   *
   * @param projectId
   * @param requestCreateTestDto
   * @return
   */
  public void saveTest(Integer projectId, RequestCreateTestDto requestCreateTestDto) {
    log.info("Saving test with name: {}", requestCreateTestDto.getName());

    // 1. 테스트 저장
    Test test =
        Test.builder()
            .name(requestCreateTestDto.getName())
            .difficultyLevel(requestCreateTestDto.getDifficultyLevel())
            .limitedTime(requestCreateTestDto.getLimitedTime())
            .passScore(requestCreateTestDto.getPassScore())
            .isRetake(requestCreateTestDto.getIsRetake())
            .isDeleted(false)
            .project(projectRepository.findById(projectId).orElseThrow())
            .build();
    testRepository.save(test);

    // 2. 병렬로 문제 생성 및 TestQuestion 저장
    generateAndSaveQuestionsInParallel(test, requestCreateTestDto, projectId);

    // 3. 초대 링크 생성
    String token = UUID.randomUUID().toString();
    LocalDateTime expiration = LocalDateTime.now().plusDays(7);

    InviteLink inviteLink =
        InviteLink.builder().test(test).token(token).expiresAt(expiration).isDeleted(false).build();
    inviteLinkRepository.save(inviteLink);
  }

  /**
   * 테스트 ID로 초대 링크를 조회합니다.
   *
   * @param testId
   * @return 초대 링크 URL
   */
  public String getInviteLink(Integer testId) {
    log.info("Fetching invite link for test ID: {}", testId);

    Test test =
        testRepository
            .findById(testId)
            .orElseThrow(() -> new IllegalArgumentException("해당 테스트를 찾을 수 없습니다: " + testId));

    InviteLink inviteLink =
        inviteLinkRepository
            .findByTest_TestIdAndIsDeletedFalse(test.getTestId())
            .orElseThrow(() -> new IllegalArgumentException("해당 테스트의 초대 링크를 찾을 수 없습니다."));

    return "https://localhost:8080/invite/" + inviteLink.getToken();
  }

  /**
   * 유저 ID로 유저의 테스트 목록을 조회합니다.
   *
   * @param userId
   * @return ResponseTestListDto
   */
  public ResponseTestListDto getUserTestList(Integer userTestId) {
    log.info("Fetching user test list for user ID: {}", userTestId);

    List<UserTest> userTests = userTestRepository.findAllById(List.of(userTestId));

    if (userTests.isEmpty()) {
      throw new IllegalArgumentException("해당 유저의 테스트가 없습니다: " + userTestId);
    }

    List<ResponseTestDto> responseList =
        userTests.stream()
            .map(userTest -> getTestByUserTestId(userTest.getUserTestId()))
            .collect(Collectors.toList());

    return new ResponseTestListDto(responseList.size(), responseList);
  }

  /**
   * 유저 테스트 ID로 테스트 정보를 조회합니다.
   *
   * @param userTestId
   * @return
   */
  public ResponseTestDto getTestByUserTestId(Integer userTestId) {
    log.info("Fetching test with ID: {}", userTestId);

    UserTest userTest =
        userTestRepository
            .findById(userTestId)
            .orElseThrow(() -> new IllegalArgumentException("해당 유저테스트를 찾을 수 없습니다: " + userTestId));

    Test test =
        testRepository
            .findById(userTest.getTest().getTestId())
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "해당 테스트를 찾을 수 없습니다: " + userTest.getTest().getTestId()));

    // 1. TestQuestion → questionId 수집
    List<TestQuestion> testQuestions = testQuestionRepository.findByTest(test);
    List<String> questionIds =
        testQuestions.stream().map(TestQuestion::getQuestionId).collect(Collectors.toList());

    // 2. MongoDB에서 실제 문제 조회
    List<Question> questions = questionMongoRepository.findAllById(questionIds);

    // 3. Question → QuestionDto 변환
    List<QuestionDto> questionDtos =
        questions.stream().map(questionDtoConverter::convert).collect(Collectors.toList());

    // 4. Test → ResponseTestDto 변환
    ResponseTestDto responseDto = testDtoConverter.convert(test); // 기존 converter 사용
    responseDto.setQuestions(questionDtos); // 문제 리스트 추가

    return responseDto;
  }

  /**
   * 테스트 ID로 테스트 정보를 조회합니다.
   *
   * @param testId 테스트 ID
   * @param lang 사용 언어 코드 (예: "ko", "en", "vi")
   * @return ResponseTestDto (문제 리스트 포함, 일부 필드 번역 적용)
   */
  public ResponseTestDto getTestById(Integer testId, String lang) {
    log.info("Fetching test with ID: {}", testId);

    Test test =
        testRepository
            .findById(testId)
            .orElseThrow(() -> new IllegalArgumentException("해당 테스트를 찾을 수 없습니다: " + testId));

    // 1. TestQuestion → questionId 수집
    List<TestQuestion> testQuestions = testQuestionRepository.findByTest(test);
    List<String> questionIds =
        testQuestions.stream().map(TestQuestion::getQuestionId).collect(Collectors.toList());

    // 2. MongoDB에서 실제 문제 조회
    List<Question> questions = questionMongoRepository.findAllById(questionIds);

    // 3. Question → QuestionDto 변환 + 조건적 번역
    List<QuestionDto> questionDtos =
        questions.stream()
            .map(questionDtoConverter::convert)
            .map(
                questionDto -> {
                  if (!"ko".equalsIgnoreCase(lang)) {
                    return questionTranslator.translateQuestionDto(questionDto, lang); // 🔧 수정
                  }
                  return questionDto;
                })
            .collect(Collectors.toList());

    // 4. Test → ResponseTestDto 변환
    ResponseTestDto responseDto = testDtoConverter.convert(test);
    responseDto.setQuestions(questionDtos);

    return responseDto;
  }

  /**
   * 특정 프로젝트의 모든 테스트를 조회합니다.
   *
   * @param projectId
   * @return ResponseTestListDto
   */
  public ResponseTestListDto getAllTests(Integer projectId) {
    log.info("Fetching all tests for project ID: {}", projectId);
    var tests = testRepository.findByProject_ProjectId(projectId);

    DtoConverter<Test, ResponseTestDto> converter = new TestDtoConverter();

    var resultList = tests.stream().map(converter::convert).toList();

    return new ResponseTestListDto(resultList.size(), resultList);
  }

  /**
   * 테스트 ID로 테스트를 삭제합니다.
   *
   * @param testId
   */
  public void deleteTest(Integer testId) {
    log.info("Deleting test with ID: {}", testId);
    Test test =
        testRepository
            .findById(testId)
            .orElseThrow(() -> new IllegalArgumentException("해당 테스트를 찾을 수 없습니다: " + testId));

    test.setIsDeleted(true);
    testRepository.save(test);
    log.info("Test deleted successfully: {}", test.getName());
  }

  /**
   * 초대 링크와 이메일을 기반으로 유저를 유저테스트에 등록합니다.
   *
   * @param token 초대 토큰
   * @param email 유저 이메일
   */
  public void registerUserToTest(String token, Integer userId) {
    // 1. 초대 토큰으로 InviteLink 찾기
    InviteLink inviteLink =
        inviteLinkRepository
            .findByTokenAndIsDeletedFalse(token)
            .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 초대 토큰입니다."));

    if (inviteLink.getExpiresAt().isBefore(LocalDateTime.now())) {
      throw new IllegalArgumentException("초대 링크가 만료되었습니다.");
    }

    // 2. 해당 테스트
    Test test = inviteLink.getTest();

    // 3. 이메일로 유저 조회
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("해당 유저가 존재하지 않습니다."));

    // 4. 이미 등록된 UserTest 있는지 확인
    Optional<UserTest> existing =
        userTestRepository.findByTest_TestIdAndUser_UserIdAndIsDeletedFalse(
            test.getTestId(), user.getUserId());

    if (existing.isPresent()) {
      log.info("이미 등록된 유저입니다.");
      return;
    }

    // 5. 새로운 UserTest 등록
    UserTest userTest =
        UserTest.builder()
            .test(test)
            .user(user)
            .isTaken(false)
            .retake(false)
            .isPassed(false)
            .takenDate(LocalDateTime.now())
            .score(0)
            .isDeleted(false)
            .build();

    userTestRepository.save(userTest);
  }

  private void generateAndSaveQuestionsInParallel(
      Test test, RequestCreateTestDto requestDto, Integer projectId) {
    ExecutorService executor = Executors.newFixedThreadPool(4);
    List<Future<List<Question>>> futures = new ArrayList<>();

    for (TestDocumentConfigDto config : requestDto.getDocumentConfigs()) {
      futures.add(
          executor.submit(
              () -> {
                RequestCreateQuestionDto dto =
                    RequestCreateQuestionDto.builder()
                        .name(requestDto.getName())
                        .difficultyLevel(requestDto.getDifficultyLevel())
                        .limitedTime(requestDto.getLimitedTime())
                        .passScore(requestDto.getPassScore())
                        .isRetake(requestDto.getIsRetake())
                        .documentId(config.getDocumentId())
                        .configuredObjectiveCount(config.getConfiguredObjectiveCount())
                        .configuredSubjectiveCount(config.getConfiguredSubjectiveCount())
                        .build();

                return questionService.generateQuestions(List.of(dto), projectId);
              }));
    }

    try {
      for (Future<List<Question>> future : futures) {
        List<Question> questions = future.get(); // 한 문서에서 생성된 문제들

        for (Question q : questions) {
          TestQuestion testQuestion =
              TestQuestion.builder()
                  .test(test)
                  .questionId(q.getId()) // MongoDB ObjectId
                  .isDeleted(false)
                  .build();
          testQuestionRepository.save(testQuestion);
        }
      }
    } catch (InterruptedException | ExecutionException e) {
      log.error("문제 병렬 생성 중 오류 발생", e);
      throw new RuntimeException("문제 생성 실패", e);
    } finally {
      executor.shutdown();
    }
  }
}

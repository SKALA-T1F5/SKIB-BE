package com.t1f5.skib.test.service;

import com.t1f5.skib.document.domain.Document;
import com.t1f5.skib.document.domain.Summary;
import com.t1f5.skib.document.dto.SummaryDto;
import com.t1f5.skib.document.repository.DocumentRepository;
import com.t1f5.skib.document.repository.SummaryMongoRepository;
import com.t1f5.skib.global.dtos.DtoConverter;
import com.t1f5.skib.global.enums.QuestionType;
import com.t1f5.skib.project.repository.ProjectJpaRepository;
import com.t1f5.skib.question.domain.DocumentQuestion;
import com.t1f5.skib.question.domain.Question;
import com.t1f5.skib.question.dto.QuestionDto;
import com.t1f5.skib.question.dto.RequestCreateQuestionDto;
import com.t1f5.skib.question.dto.ResponseQuestionDtoConverter;
import com.t1f5.skib.question.repository.DocumentQuestionRepository;
import com.t1f5.skib.question.repository.QuestionMongoRepository;
import com.t1f5.skib.question.service.QuestionService;
import com.t1f5.skib.test.domain.InviteLink;
import com.t1f5.skib.test.domain.Test;
import com.t1f5.skib.test.domain.TestDocumentConfig;
import com.t1f5.skib.test.domain.TestQuestion;
import com.t1f5.skib.test.domain.UserTest;
import com.t1f5.skib.test.dto.QuestionTranslator;
import com.t1f5.skib.test.dto.RequestCreateTestByLLMDto;
import com.t1f5.skib.test.dto.RequestCreateTestDto;
import com.t1f5.skib.test.dto.ResponseTestDto;
import com.t1f5.skib.test.dto.ResponseTestListDto;
import com.t1f5.skib.test.dto.ResponseTestSummaryDto;
import com.t1f5.skib.test.dto.ResponseTestSummaryListDto;
import com.t1f5.skib.test.dto.TestDocumentConfigDto;
import com.t1f5.skib.test.dto.TestDtoConverter;
import com.t1f5.skib.test.repository.InviteLinkRepository;
import com.t1f5.skib.test.repository.TestDocumentConfigRepository;
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
import org.apache.commons.lang3.tuple.Pair;
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
  private final DocumentRepository documentRepository;
  private final SummaryMongoRepository summaryMongoRepository;
  private final TestDocumentConfigRepository testDocumentConfigRepository;
  private final DocumentQuestionRepository documentQuestionRepository;
  @Autowired private QuestionTranslator questionTranslator;

  /**
   * LLM을 사용하여 테스트를 생성합니다.
   *
   * @param projectId 프로젝트 ID
   * @param dto 사용자 입력 및 요약 정보 DTO
   * @return 생성된 테스트의 응답
   */
  public String makeTest(Integer projectId, RequestCreateTestByLLMDto dto) {
    log.info("Creating test by LLM for project ID: {}", projectId);

    // 1. 프로젝트 존재 확인
    if (!projectRepository.existsById(projectId)) {
      throw new IllegalArgumentException("해당 프로젝트가 존재하지 않습니다: " + projectId);
    }

    // 2. DBMS에서 프로젝트에 속한 문서 목록 조회
    List<Document> documents =
        documentRepository.findByProject_ProjectIdAndIsDeletedFalse(projectId);
    List<Integer> documentIds =
        documents.stream().map(Document::getDocumentId).collect(Collectors.toList());

    // 3. MongoDB에서 문서 요약 정보 조회
    List<Summary> summaries = summaryMongoRepository.findByDocumentIdIn(documentIds);

    // 4. Summary → SummaryDto 변환
    List<SummaryDto> summaryDtos =
        summaries.stream()
            .map(
                summary ->
                    SummaryDto.builder()
                        .documentId(summary.getDocumentId())
                        .summary(summary.getSummary())
                        .keywords(summary.getKeywords())
                        .build())
            .collect(Collectors.toList());

    // 5. FastAPI로 보낼 DTO 구성
    RequestCreateTestByLLMDto payload =
        new RequestCreateTestByLLMDto(projectId, dto.getUserInput(), summaryDtos);

    // 6. FastAPI 호출
    String response =
        webClient
            .post()
            .uri("http://skib-ai.skala25a.project.skala-ai.com/api/test/plan")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(payload)
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
            .summary(requestCreateTestDto.getSummary())
            .difficultyLevel(requestCreateTestDto.getDifficultyLevel())
            .limitedTime(requestCreateTestDto.getLimitedTime())
            .passScore(requestCreateTestDto.getPassScore())
            .isRetake(requestCreateTestDto.getIsRetake())
            .isDeleted(false)
            .project(
                projectRepository
                    .findById(projectId)
                    .orElseThrow(() -> new IllegalArgumentException("프로젝트가 존재하지 않습니다.")))
            .build();
    testRepository.save(test);

    // 2. 병렬로 문제 생성 및 연관 테이블 저장
    generateAndSaveQuestionsInParallel(test, requestCreateTestDto);

    // ✅ 3. 문서 요약 정보 조회 (MongoDB)
    List<Integer> documentIds =
        requestCreateTestDto.getDocumentConfigs().stream()
            .map(TestDocumentConfigDto::getDocumentId)
            .collect(Collectors.toList());

    List<Summary> summaries = summaryMongoRepository.findByDocumentIdIn(documentIds);

    List<SummaryDto> summaryDtos =
        summaries.stream()
            .map(
                summary ->
                    SummaryDto.builder()
                        .documentId(summary.getDocumentId())
                        .summary(summary.getSummary())
                        .keywords(summary.getKeywords())
                        .build())
            .collect(Collectors.toList());

    // ✅ 4. FastAPI에 전달할 요청 객체 생성
    RequestCreateTestByLLMDto payload =
        new RequestCreateTestByLLMDto(
            projectId,
            requestCreateTestDto.getSummary(), // userInput 대신 summary 사용
            summaryDtos);

    String response =
        webClient
            .post()
            .uri("http://skib-ai.skala25a.project.skala-ai.com/api/test/generate")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(payload)
            .retrieve()
            .bodyToMono(String.class)
            .block();

    log.info("FastAPI 응답: {}", response);

    // 5. 초대 링크 생성 및 저장
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

    return "https://skib-backend.skala25a.project.skala-ai.com/invite/" + inviteLink.getToken();
  }

  /**
   * 유저 ID로 유저의 테스트 목록을 조회합니다.
   *
   * @param userId
   * @return ResponseTestListDto
   */
  public ResponseTestSummaryListDto getUserTestList(Integer userId) {
    log.info("Fetching user test list for user ID: {}", userId);

    List<UserTest> userTests = userTestRepository.findAllByUser_UserIdAndIsDeletedFalse(userId);

    if (userTests.isEmpty()) {
      throw new IllegalArgumentException("해당 유저의 테스트가 없습니다: " + userId);
    }

    List<ResponseTestSummaryDto> responseList =
        userTests.stream()
            .map(
                userTest -> {
                  Test test = userTest.getTest();
                  return ResponseTestSummaryDto.builder()
                      .testId(test.getTestId())
                      .name(test.getName())
                      .difficultyLevel(test.getDifficultyLevel())
                      .isPassed(userTest.getIsPassed())
                      .retake(userTest.getRetake())
                      .score(userTest.getScore())
                      .isRetake(test.getIsRetake())
                      .passScore(test.getPassScore())
                      .limitedTime(test.getLimitedTime())
                      .createdAt(test.getCreatedDate())
                      .build();
                })
            .collect(Collectors.toList());

    return new ResponseTestSummaryListDto(responseList.size(), responseList);
  }

  /**
   * 유저 ID와 테스트 ID로 테스트 정보를 조회합니다.
   *
   * @param userId 유저 ID
   * @param testId 테스트 ID
   * @return ResponseTestDto (문제 리스트 포함)
   */
  public ResponseTestDto getTestByUserTestId(Integer userId, Integer testId) {
    log.info("Fetching test with ID: {}", userId, testId);

    UserTest userTest =
        userTestRepository
            .findByUser_UserIdAndTest_TestIdAndIsDeletedFalse(userId, testId)
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "해당 유저테스트를 찾을 수 없습니다: userId=" + userId + ", testId=" + testId));

    // ✅ 재응시(retake)가 false일 때만 허용
    if (Boolean.TRUE.equals(userTest.getRetake())) {
      throw new IllegalStateException("해당 테스트는 재응시 상태입니다. 접근할 수 없습니다.");
    }

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
    ResponseTestDto responseDto = testDtoConverter.convert(test);
    responseDto.setQuestions(questionDtos);

    responseDto.setPassScore(test.getPassScore());

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
  public ResponseTestDto registerUserToTestAndReturnTest(
      String token, Integer userId, String lang) {
    // 1. 초대 토큰으로 InviteLink 찾기
    InviteLink inviteLink =
        inviteLinkRepository
            .findByTokenAndIsDeletedFalse(token)
            .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 초대 토큰입니다."));

    if (inviteLink.getExpiresAt().isBefore(LocalDateTime.now())) {
      throw new IllegalArgumentException("초대 링크가 만료되었습니다.");
    }

    // 2. 테스트 및 유저 조회
    Test test = inviteLink.getTest();
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("해당 유저가 존재하지 않습니다."));

    // 3. 이미 등록된 UserTest 있는지 확인
    Optional<UserTest> existing =
        userTestRepository.findByTest_TestIdAndUser_UserIdAndIsDeletedFalse(
            test.getTestId(), user.getUserId());

    UserTest userTest;

    if (existing.isPresent()) {
      log.info("이미 등록된 유저입니다.");
      userTest = existing.get();
    } else {
      // 4. 새로 등록
      userTest =
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

    // 5. 테스트 + 문제 리스트 함께 반환
    return getTestById(userId, lang);
  }

  private void generateAndSaveQuestionsInParallel(Test test, RequestCreateTestDto requestDto) {
    ExecutorService executor = Executors.newFixedThreadPool(4);
    List<Future<Pair<TestDocumentConfigDto, List<Question>>>> futures = new ArrayList<>();

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

                List<Question> questions = questionService.generateQuestions(List.of(dto));
                return Pair.of(config, questions);
              }));
    }

    try {
      for (Future<Pair<TestDocumentConfigDto, List<Question>>> future : futures) {
        Pair<TestDocumentConfigDto, List<Question>> result = future.get();

        TestDocumentConfigDto config = result.getLeft();
        List<Question> questions = result.getRight();

        // 1. 각 문제를 TestQuestion에 저장
        for (Question q : questions) {
          TestQuestion testQuestion =
              TestQuestion.builder().test(test).questionId(q.getId()).isDeleted(false).build();
          testQuestionRepository.save(testQuestion);
        }

        // 2. TestDocumentConfig 저장
        TestDocumentConfig testDocumentConfig =
            TestDocumentConfig.builder()
                .test(test)
                .document(documentRepository.findById(config.getDocumentId()).orElseThrow())
                .configuredObjectiveCount(config.getConfiguredObjectiveCount())
                .configuredSubjectiveCount(config.getConfiguredSubjectiveCount())
                .isDeleted(false)
                .build();
        testDocumentConfigRepository.save(testDocumentConfig);

        // 3. DocumentQuestion 저장
        int objectiveCount =
            (int) questions.stream().filter(q -> q.getType() == QuestionType.OBJECTIVE).count();
        int subjectiveCount =
            (int) questions.stream().filter(q -> q.getType() == QuestionType.SUBJECTIVE).count();

        QuestionType questionType;
        if (objectiveCount > 0) {
          questionType = QuestionType.OBJECTIVE;
        } else {
          questionType = QuestionType.SUBJECTIVE;
        }

        DocumentQuestion documentQuestion =
            DocumentQuestion.builder()
                .document(documentRepository.findById(config.getDocumentId()).orElseThrow())
                .questionKey(UUID.randomUUID().toString())
                .questionType(questionType)
                .configuredObjectiveCount(objectiveCount)
                .configuredSubjectiveCount(subjectiveCount)
                .isDeleted(false)
                .build();

        documentQuestionRepository.save(documentQuestion);
      }

    } catch (InterruptedException | ExecutionException e) {
      log.error("문제 병렬 생성 중 오류 발생", e);
      throw new RuntimeException("문제 생성 실패", e);
    } finally {
      executor.shutdown();
    }
  }
}

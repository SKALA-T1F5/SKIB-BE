package com.t1f5.skib.test.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import com.t1f5.skib.question.dto.ResponseQuestionDtoConverter;
import com.t1f5.skib.question.repository.DocumentQuestionRepository;
import com.t1f5.skib.question.repository.QuestionMongoRepository;
import com.t1f5.skib.question.service.QuestionService;
import com.t1f5.skib.test.domain.InviteLink;
import com.t1f5.skib.test.domain.Test;
import com.t1f5.skib.test.domain.TestDocumentConfig;
import com.t1f5.skib.test.domain.TestQuestion;
import com.t1f5.skib.test.domain.UserTest;
import com.t1f5.skib.test.dto.DocumentQuestionCountDto;
import com.t1f5.skib.test.dto.QuestionTranslator;
import com.t1f5.skib.test.dto.RequestCreateTestByLLMDto;
import com.t1f5.skib.test.dto.RequestCreateTestDto;
import com.t1f5.skib.test.dto.RequestFinalizeTestDto;
import com.t1f5.skib.test.dto.RequestSaveRandomTestDto;
import com.t1f5.skib.test.dto.ResponseCreateTestByLLMDto;
import com.t1f5.skib.test.dto.ResponseTestDto;
import com.t1f5.skib.test.dto.ResponseTestInitDto;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

  @Value("${fastapi.base-url}")
  private String fastApiBaseUrl;

  /**
   * LLM을 사용하여 테스트를 생성합니다.
   *
   * @param projectId 프로젝트 ID
   * @param dto 사용자 입력 및 요약 정보 DTO
   * @return 생성된 테스트의 응답
   */
  public ResponseCreateTestByLLMDto makeTest(Integer projectId, String userInput) {
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
                        .name(summary.getName())
                        .summary(summary.getSummary())
                        .keywords(summary.getKeywords())
                        .build())
            .collect(Collectors.toList());

    // 5. FastAPI로 보낼 DTO 구성
    RequestCreateTestByLLMDto payload =
        new RequestCreateTestByLLMDto(projectId, userInput, summaryDtos);

    ObjectMapper mapper = new ObjectMapper();
    try {
      String jsonPayload = mapper.writeValueAsString(payload);
      log.info("📤 FastAPI로 전송할 payload JSON:\n{}", jsonPayload);
    } catch (JsonProcessingException e) {
      log.error("❌ payload 직렬화 실패", e);
    }

    // 6. FastAPI 호출
    ResponseCreateTestByLLMDto response =
        webClient
            .post()
            .uri(fastApiBaseUrl + "/api/test/plan")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(payload)
            .retrieve()
            .bodyToMono(ResponseCreateTestByLLMDto.class)
            .block(); // Blocking 방식으로 응답 대기

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
  public ResponseTestInitDto saveTestWithQuestions(Integer projectId, RequestCreateTestDto dto) {
    // 테스트 저장
    Test test =
        Test.builder()
            .name(dto.getName())
            .summary(dto.getSummary())
            .difficultyLevel(dto.getDifficultyLevel())
            .limitedTime(dto.getLimitedTime())
            .passScore(dto.getPassScore())
            .isRetake(dto.getIsRetake())
            .isDeleted(false)
            .project(projectRepository.findById(projectId).orElseThrow())
            .build();
    testRepository.save(test);

    // 문제 생성 및 MongoDB 저장
    List<Question> generatedQuestions = questionService.generateQuestions(dto);

    // TestDocumentConfig만 저장 (DocumentQuestion은 나중에)
    for (TestDocumentConfigDto config : dto.getDocumentConfigs()) {
      TestDocumentConfig testDocumentConfig =
          TestDocumentConfig.builder()
              .test(test)
              .document(documentRepository.findById(config.getDocumentId()).orElseThrow())
              .configuredObjectiveCount(config.getConfiguredObjectiveCount())
              .configuredSubjectiveCount(config.getConfiguredSubjectiveCount())
              .isDeleted(false)
              .build();
      testDocumentConfigRepository.save(testDocumentConfig);
    }

    return ResponseTestInitDto.builder()
        .testId(test.getTestId())
        .questions(generatedQuestions)
        .build();
  }

  /**
   * 테스트를 최종화하고 초대 링크를 생성합니다.
   *
   * @param dto
   */
  @Transactional
  public String finalizeTest(Integer testId, RequestFinalizeTestDto dto) {
    Test test =
        testRepository
            .findById(testId)
            .orElseThrow(() -> new IllegalArgumentException("테스트가 존재하지 않습니다."));

    List<Question> selectedQuestions =
        questionMongoRepository.findAllById(dto.getSelectedQuestionIds());

    // ✅ TestQuestion 저장
    int questionNumber = 1;
    for (Question q : selectedQuestions) {
      testQuestionRepository.save(
          TestQuestion.builder()
              .test(test)
              .questionId(q.getId())
              .questionNumber(questionNumber++)
              .isDeleted(false)
              .build());
    }

    // ✅ DocumentQuestion 저장
    Map<String, List<Question>> groupedByDocument =
        selectedQuestions.stream().collect(Collectors.groupingBy(Question::getDocumentId));

    for (Map.Entry<String, List<Question>> entry : groupedByDocument.entrySet()) {
      String documentIdStr = entry.getKey();
      List<Question> questionsForDoc = entry.getValue();

      Integer documentId;
      try {
        documentId = Integer.parseInt(documentIdStr);
      } catch (NumberFormatException e) {
        log.warn("문서 ID가 정수가 아닙니다: {}", documentIdStr);
        continue;
      }

      Document document =
          documentRepository
              .findById(documentId)
              .orElseThrow(() -> new IllegalArgumentException("문서를 찾을 수 없습니다: " + documentId));

      int objCount =
          (int) questionsForDoc.stream().filter(q -> q.getType() == QuestionType.OBJECTIVE).count();

      int subCount =
          (int)
              questionsForDoc.stream().filter(q -> q.getType() == QuestionType.SUBJECTIVE).count();

      if (objCount + subCount == 0) continue;

      QuestionType type = objCount > 0 ? QuestionType.OBJECTIVE : QuestionType.SUBJECTIVE;

      documentQuestionRepository.save(
          DocumentQuestion.builder()
              .document(document)
              .questionKey(UUID.randomUUID().toString())
              .questionType(type)
              .configuredObjectiveCount(objCount)
              .configuredSubjectiveCount(subCount)
              .isDeleted(false)
              .build());
    }

    // ✅ 초대 링크 생성 및 반환
    String token = UUID.randomUUID().toString();
    InviteLink inviteLink =
        InviteLink.builder()
            .test(test)
            .token(token)
            .expiresAt(LocalDateTime.now().plusDays(7))
            .isDeleted(false)
            .build();
    inviteLinkRepository.save(inviteLink);

    // ✅ 여분 문제 삭제
    if (dto.getToDeleteQuestionIds() != null && !dto.getToDeleteQuestionIds().isEmpty()) {
      questionMongoRepository.deleteAllById(dto.getToDeleteQuestionIds());
    }

    return token;
  }

  /**
   * 랜덤 테스트를 저장합니다.
   *
   * @param dto
   */
  @Transactional
  public void saveRandomTest(RequestSaveRandomTestDto dto) {
    // 1. 프로젝트 확인
    var project =
        projectRepository
            .findById(dto.getProjectId())
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 프로젝트입니다."));

    // 2. 테스트 저장
    Test test =
        Test.builder()
            .name(dto.getName())
            .limitedTime(dto.getLimitedTime())
            .passScore(dto.getPassScore())
            .summary("랜덤 테스트") // 또는 프론트에서 받아도 OK
            .isRetake(false)
            .isDeleted(false)
            .project(project)
            .build();
    testRepository.save(test);

    // 3. MongoDB에서 문제 조회
    List<Question> questions = questionMongoRepository.findAllById(dto.getQuestionIds());

    // 4. TestQuestion 저장
    for (Question q : questions) {
      TestQuestion tq =
          TestQuestion.builder().test(test).questionId(q.getId()).isDeleted(false).build();
      testQuestionRepository.save(tq);
    }

    // 5. 문서별로 objective/subjective 문제 수 계산 후 TestDocumentConfig + DocumentQuestion 저장
    Map<String, List<Question>> groupedByDoc =
        questions.stream().collect(Collectors.groupingBy(Question::getDocumentId));

    for (String docIdStr : groupedByDoc.keySet()) {
      int docId = Integer.parseInt(docIdStr);
      Document document =
          documentRepository
              .findById(docId)
              .orElseThrow(() -> new IllegalArgumentException("문서를 찾을 수 없습니다: " + docId));

      List<Question> docQuestions = groupedByDoc.get(docIdStr);
      int objective =
          (int) docQuestions.stream().filter(q -> q.getType() == QuestionType.OBJECTIVE).count();
      int subjective =
          (int) docQuestions.stream().filter(q -> q.getType() == QuestionType.SUBJECTIVE).count();

      // TestDocumentConfig 저장
      testDocumentConfigRepository.save(
          TestDocumentConfig.builder()
              .test(test)
              .document(document)
              .configuredObjectiveCount(objective)
              .configuredSubjectiveCount(subjective)
              .isDeleted(false)
              .build());

      // DocumentQuestion 저장
      QuestionType questionType = objective > 0 ? QuestionType.OBJECTIVE : QuestionType.SUBJECTIVE;

      documentQuestionRepository.save(
          DocumentQuestion.builder()
              .document(document)
              .questionKey(UUID.randomUUID().toString())
              .questionType(questionType)
              .configuredObjectiveCount(objective)
              .configuredSubjectiveCount(subjective)
              .isDeleted(false)
              .build());
    }

    log.info("✅ 랜덤 테스트 저장 완료: {}", test.getName());
  }

  /**
   * 프로젝트 ID와 총 문제 수를 기반으로 랜덤 테스트를 생성합니다.
   *
   * @param projectId 프로젝트 ID
   * @param totalCount 생성할 총 문제 수
   * @return 랜덤으로 선택된 문제 리스트
   */
  public List<Question> generateRandomTest(int projectId, int totalCount) {
    // 1. 프로젝트에 해당하는 문서들 조회
    List<Document> documents = documentRepository.findAllByProject_ProjectId(projectId);

    // 2. 각 문서별 DocumentQuestion 수 집계
    Map<Integer, List<DocumentQuestion>> questionMap = new HashMap<>();
    int totalAvailableQuestions = 0;

    for (Document doc : documents) {
      List<DocumentQuestion> dqList =
          documentQuestionRepository.findByDocument_DocumentId(doc.getDocumentId());
      questionMap.put(doc.getDocumentId(), dqList);
      totalAvailableQuestions += dqList.size();
    }

    if (totalAvailableQuestions < totalCount) {
      throw new IllegalArgumentException("총 문제 수가 부족합니다.");
    }

    // 3. 문서별 비율로 문제 수 배정
    Map<Integer, Integer> questionCountPerDoc = new HashMap<>();
    for (Document doc : documents) {
      int count = questionMap.get(doc.getDocumentId()).size();
      int docQuestionCount = Math.round(((float) count / totalAvailableQuestions) * totalCount);
      questionCountPerDoc.put(doc.getDocumentId(), docQuestionCount);
    }

    // 보정: 합이 totalCount 안 맞을 수 있으므로 가장 많은 문서에 부족분 보정
    int sum = questionCountPerDoc.values().stream().mapToInt(i -> i).sum();
    if (sum != totalCount) {
      int delta = totalCount - sum;
      Integer maxDocId =
          Collections.max(questionCountPerDoc.entrySet(), Map.Entry.comparingByValue()).getKey();
      questionCountPerDoc.put(maxDocId, questionCountPerDoc.get(maxDocId) + delta);
    }

    // 4. 각 문서에서 랜덤으로 문제 선택
    List<String> selectedQuestionKeys = new ArrayList<>();
    for (Document doc : documents) {
      List<DocumentQuestion> candidates = questionMap.get(doc.getDocumentId());
      Collections.shuffle(candidates);
      int pick = questionCountPerDoc.get(doc.getDocumentId());
      selectedQuestionKeys.addAll(
          candidates.stream().limit(pick).map(DocumentQuestion::getQuestionKey).toList());
    }

    // 5. MongoDB에서 문제 조회
    return questionMongoRepository.findAllById(selectedQuestionKeys);
  }

  /**
   * 프로젝트 ID에 따른 문서의 문제 수를 조회합니다.
   *
   * @param projectId 프로젝트 ID
   * @return 문서별 문제 수 리스트
   */
  public List<DocumentQuestionCountDto> getDocumentQuestionCountsByProject(Integer projectId) {
    List<Document> documents = documentRepository.findAllByProject_ProjectId(projectId);

    return documents.stream()
        .map(
            doc -> {
              int count =
                  documentQuestionRepository.countByDocument_DocumentId(doc.getDocumentId());
              return new DocumentQuestionCountDto(doc.getDocumentId(), doc.getName(), count);
            })
        .collect(Collectors.toList());
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

  // private void generateAndSaveQuestionsInParallel(Test test, RequestCreateTestDto requestDto) {
  //   ExecutorService executor = Executors.newFixedThreadPool(4);
  //   List<Future<Pair<TestDocumentConfigDto, List<Question>>>> futures = new ArrayList<>();

  //   for (TestDocumentConfigDto config : requestDto.getDocumentConfigs()) {
  //     futures.add(
  //         executor.submit(
  //             () -> {
  //               RequestCreateQuestionDto dto =
  //                   RequestCreateQuestionDto.builder()
  //                       .name(requestDto.getName())
  //                       .summary(requestDto.getSummary())
  //                       .difficultyLevel(requestDto.getDifficultyLevel())
  //                       .limitedTime(requestDto.getLimitedTime())
  //                       .passScore(requestDto.getPassScore())
  //                       .isRetake(requestDto.getIsRetake())
  //                       .documentId(config.getDocumentId())
  //                       .keywords(config.getKeywords())
  //                       .configuredObjectiveCount(config.getConfiguredObjectiveCount())
  //                       .configuredSubjectiveCount(config.getConfiguredSubjectiveCount())
  //                       .build();

  //               List<Question> questions = questionService.generateQuestions(List.of(dto));
  //               return Pair.of(config, questions);
  //             }));
  //   }

  //   try {
  //     for (Future<Pair<TestDocumentConfigDto, List<Question>>> future : futures) {
  //       Pair<TestDocumentConfigDto, List<Question>> result = future.get();

  //       TestDocumentConfigDto config = result.getLeft();
  //       List<Question> questions = result.getRight();

  //       // 1. 각 문제를 TestQuestion에 저장
  //       for (Question q : questions) {
  //         TestQuestion testQuestion =
  //             TestQuestion.builder().test(test).questionId(q.getId()).isDeleted(false).build();
  //         testQuestionRepository.save(testQuestion);
  //       }

  //       // 2. TestDocumentConfig 저장
  //       TestDocumentConfig testDocumentConfig =
  //           TestDocumentConfig.builder()
  //               .test(test)
  //               .document(documentRepository.findById(config.getDocumentId()).orElseThrow())
  //               .configuredObjectiveCount(config.getConfiguredObjectiveCount())
  //               .configuredSubjectiveCount(config.getConfiguredSubjectiveCount())
  //               .isDeleted(false)
  //               .build();
  //       testDocumentConfigRepository.save(testDocumentConfig);

  //       // 3. DocumentQuestion 저장
  //       int objectiveCount =
  //           (int) questions.stream().filter(q -> q.getType() == QuestionType.OBJECTIVE).count();
  //       int subjectiveCount =
  //           (int) questions.stream().filter(q -> q.getType() == QuestionType.SUBJECTIVE).count();

  //       QuestionType questionType;
  //       if (objectiveCount > 0) {
  //         questionType = QuestionType.OBJECTIVE;
  //       } else {
  //         questionType = QuestionType.SUBJECTIVE;
  //       }

  //       DocumentQuestion documentQuestion =
  //           DocumentQuestion.builder()
  //               .document(documentRepository.findById(config.getDocumentId()).orElseThrow())
  //               .questionKey(UUID.randomUUID().toString())
  //               .questionType(questionType)
  //               .configuredObjectiveCount(objectiveCount)
  //               .configuredSubjectiveCount(subjectiveCount)
  //               .isDeleted(false)
  //               .build();

  //       documentQuestionRepository.save(documentQuestion);
  //     }

  //   } catch (InterruptedException | ExecutionException e) {
  //     log.error("문제 병렬 생성 중 오류 발생", e);
  //     throw new RuntimeException("문제 생성 실패", e);
  //   } finally {
  //     executor.shutdown();
  //   }
  // }
}

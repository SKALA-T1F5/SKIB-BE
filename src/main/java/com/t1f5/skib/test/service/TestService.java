package com.t1f5.skib.test.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.t1f5.skib.answer.service.AnswerService;
import com.t1f5.skib.document.domain.Document;
import com.t1f5.skib.document.domain.Summary;
import com.t1f5.skib.document.dto.SummaryDto;
import com.t1f5.skib.document.repository.DocumentRepository;
import com.t1f5.skib.document.repository.SummaryMongoRepository;
import com.t1f5.skib.global.dtos.DtoConverter;
import com.t1f5.skib.global.enums.DocumentStatus;
import com.t1f5.skib.global.enums.QuestionType;
import com.t1f5.skib.global.enums.TestStatus;
import com.t1f5.skib.project.domain.Project;
import com.t1f5.skib.project.repository.ProjectJpaRepository;
import com.t1f5.skib.question.domain.DocumentQuestion;
import com.t1f5.skib.question.domain.Question;
import com.t1f5.skib.question.dto.QuestionDto;
import com.t1f5.skib.question.dto.QuestionToDtoConverter;
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
import com.t1f5.skib.test.dto.TestProgressNotification;
import com.t1f5.skib.test.repository.InviteLinkRepository;
import com.t1f5.skib.test.repository.TestDocumentConfigRepository;
import com.t1f5.skib.test.repository.TestQuestionRepository;
import com.t1f5.skib.test.repository.TestRepository;
import com.t1f5.skib.test.repository.UserTestRepository;
import com.t1f5.skib.user.model.User;
import com.t1f5.skib.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
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

  private final QuestionService questionService;
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
  private final DocumentRepository documentRepository;
  private final SummaryMongoRepository summaryMongoRepository;
  private final TestDocumentConfigRepository testDocumentConfigRepository;
  private final DocumentQuestionRepository documentQuestionRepository;
  private final AnswerService answerService;
  private final QuestionToDtoConverter questionToDtoConverter;
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
        documentRepository.findByProject_ProjectIdAndStatusAndIsDeletedFalse(
            projectId, DocumentStatus.SUMMARY_COMPLETED);
    List<Integer> documentIds =
        documents.stream().map(Document::getDocumentId).distinct().collect(Collectors.toList());

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
            .distinct()
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
   * 테스트 상태를 저장합니다.
   *
   * @param notification 테스트 진행 상태 알림
   */
  @Transactional
  public void saveTestStatus(TestProgressNotification notification) {
    updateTestStatus(notification.getTestId(), notification.getStatus());
    log.info(
        "Test status updated: testId={}, status={}",
        notification.getTestId(),
        notification.getStatus());
  }

  /**
   * 테스트 상태를 업데이트합니다.
   *
   * @param testId 테스트 ID
   * @param status 업데이트할 상태
   */
  @Transactional
  public void updateTestStatus(Integer testId, TestStatus status) {
    Test test =
        testRepository
            .findById(testId)
            .orElseThrow(() -> new IllegalArgumentException("테스트를 찾을 수 없습니다: " + testId));
    test.setStatus(status);
    testRepository.save(test);
  }

  /**
   * 테스트의 현재 상태를 반환합니다.
   *
   * @param testId 테스트 ID
   * @return 테스트 상태
   */
  @Transactional
  public TestStatus getTestStatus(Integer testId) {
    Test test =
        testRepository
            .findById(testId)
            .orElseThrow(() -> new IllegalArgumentException("테스트를 찾을 수 없습니다: " + testId));
    return test.getStatus();
  }

  /**
   * 테스트를 생성합니다.
   *
   * @param dto 테스트 생성 요청 DTO
   * @param projectId 프로젝트 ID
   * @return 생성된 테스트의 ID
   */
  @Transactional
  public Integer saveTest(Integer projectId, RequestCreateTestDto dto) {
    Project project =
        projectRepository
            .findById(projectId)
            .orElseThrow(() -> new IllegalArgumentException("프로젝트가 존재하지 않습니다: " + projectId));

    Test test =
        Test.builder()
            .name(dto.getName())
            .summary(dto.getSummary())
            .difficultyLevel(dto.getDifficultyLevel())
            .limitedTime(dto.getLimitedTime())
            .passScore(dto.getPassScore())
            .isRetake(dto.getIsRetake())
            .isDeleted(false)
            .status(TestStatus.TEST_GENERATION_STARTED)
            .project(project)
            .build();

    testRepository.save(test); // ID 생성

    // TestDocumentConfig 저장
    for (TestDocumentConfigDto config : dto.getDocumentConfigs()) {
      TestDocumentConfig testDocumentConfig =
          TestDocumentConfig.builder()
              .test(test)
              .document(
                  documentRepository
                      .findById(config.getDocumentId())
                      .orElseThrow(
                          () ->
                              new IllegalArgumentException(
                                  "문서를 찾을 수 없습니다: " + config.getDocumentId())))
              .configuredObjectiveCount(config.getConfiguredObjectiveCount())
              .configuredSubjectiveCount(config.getConfiguredSubjectiveCount())
              .isDeleted(false)
              .build();
      testDocumentConfigRepository.save(testDocumentConfig);
    }

    // FastAPI로 생성 요청 (testId 포함해서 전체 DTO 보냄)
    RequestCreateTestDto dtoForFastAPI =
        RequestCreateTestDto.builder()
            .testId(test.getTestId())
            .name(test.getName())
            .summary(test.getSummary())
            .difficultyLevel(test.getDifficultyLevel())
            .limitedTime(test.getLimitedTime())
            .passScore(test.getPassScore())
            .isRetake(test.getIsRetake())
            .documentConfigs(dto.getDocumentConfigs())
            .build();

    questionService.sendTestCreationRequest(dtoForFastAPI);

    return test.getTestId();
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

      if (documentIdStr == null || documentIdStr.isBlank()) {
        log.warn("문서 ID가 null 혹은 빈 문자열입니다. 해당 문제 수: {}", questionsForDoc.size());
        continue;
      }

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

      // ✅ 문제 타입 누락 여부 로깅
      for (Question q : questionsForDoc) {
        if (q.getType() == null) {
          log.warn("문제 ID {} 의 type이 null입니다. 문서 ID: {}", q.getId(), documentId);
        }
      }

      int objCount =
          (int)
              questionsForDoc.stream()
                  .filter(q -> QuestionType.OBJECTIVE.equals(q.getType()))
                  .count();

      int subCount =
          (int)
              questionsForDoc.stream()
                  .filter(q -> QuestionType.SUBJECTIVE.equals(q.getType()))
                  .count();

      log.info("📄 문서 {} - 객관식 {}개, 주관식 {}개", documentId, objCount, subCount);

      if (objCount + subCount == 0) {
        log.warn("문서 {} 에 연결된 문제 수는 있지만, 타입이 유효하지 않아 저장 생략", documentId);
        continue;
      }

      QuestionType type = objCount > 0 ? QuestionType.OBJECTIVE : QuestionType.SUBJECTIVE;

      String joinedKeys =
          String.join(
              ",",
              questionsForDoc.stream()
                  .map(Question::getId)
                  .map(String::valueOf)
                  .collect(Collectors.toList()));

      documentQuestionRepository.save(
          DocumentQuestion.builder()
              .document(document)
              .questionKey(joinedKeys)
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
  public ResponseTestInitDto saveRandomTest(RequestSaveRandomTestDto dto) {
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
            .summary("랜덤 테스트")
            .status(TestStatus.COMPLETED)
            .isRetake(false)
            .isDeleted(false)
            .project(project)
            .build();
    testRepository.save(test);

    // 3. MongoDB에서 문제 조회
    List<Question> questions = questionMongoRepository.findAllById(dto.getQuestionIds());

    // 4. TestQuestion 저장
    for (Question q : questions) {
      testQuestionRepository.save(
          TestQuestion.builder().test(test).questionId(q.getId()).isDeleted(false).build());
    }

    // 5. 문서별 그룹화 및 구성
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

      testDocumentConfigRepository.save(
          TestDocumentConfig.builder()
              .test(test)
              .document(document)
              .configuredObjectiveCount(objective)
              .configuredSubjectiveCount(subjective)
              .isDeleted(false)
              .build());


      documentQuestionRepository.save(
          DocumentQuestion.builder()
              .document(document)
              .questionKey(UUID.randomUUID().toString())
              .configuredObjectiveCount(objective)
              .configuredSubjectiveCount(subjective)
              .isDeleted(false)
              .build());
    }

    return ResponseTestInitDto.builder()
        .testId(test.getTestId())
        .questions(questions) // or `.questions(questionDtos)` if you want to return DTOs
        .build();
  }

  /**
   * 프로젝트 ID와 총 문제 수를 기반으로 랜덤 테스트를 생성합니다.
   *
   * @param projectId 프로젝트 ID
   * @param totalCount 생성할 총 문제 수
   * @return 랜덤으로 선택된 문제 리스트
   */
  @Transactional(readOnly = true)
  public List<Question> generateRandomTest(int projectId, int totalCount) {
    // 1. 문서별 생성된 문제 수 집계
    List<DocumentQuestionCountDto> docCounts = getDocumentQuestionCountsByProject(projectId);

    int totalAvailable =
        docCounts.stream().mapToInt(DocumentQuestionCountDto::getQuestionCount).sum();

    if (totalAvailable < totalCount) {
      throw new IllegalArgumentException("총 문제 수가 부족합니다.");
    }

    // 2. 비율 기반 문서별 배분
    Map<Integer, Integer> questionCountPerDoc = new HashMap<>();
    for (DocumentQuestionCountDto dto : docCounts) {
      int allocated = Math.round(((float) dto.getQuestionCount() / totalAvailable) * totalCount);
      questionCountPerDoc.put(dto.getDocumentId(), allocated);
    }

    // 3. 총합 보정
    int currentSum = questionCountPerDoc.values().stream().mapToInt(Integer::intValue).sum();
    if (currentSum != totalCount) {
      int delta = totalCount - currentSum;
      Integer maxDocId =
          Collections.max(questionCountPerDoc.entrySet(), Map.Entry.comparingByValue()).getKey();
      questionCountPerDoc.put(maxDocId, questionCountPerDoc.get(maxDocId) + delta);
    }

    // 4. 문서별로 DocumentQuestion → questionKey 파싱
    Map<Integer, List<String>> docToQuestionKeys = new HashMap<>();
    for (Integer docId : questionCountPerDoc.keySet()) {
      List<DocumentQuestion> dqList = documentQuestionRepository.findByDocument_DocumentId(docId);

      // 콤마로 분리된 questionKey를 개별 ID로 분해
      List<String> allKeys =
          dqList.stream()
              .filter(dq -> dq.getQuestionKey() != null && !dq.getQuestionKey().isBlank())
              .flatMap(dq -> Arrays.stream(dq.getQuestionKey().split(",")))
              .map(String::trim)
              .filter(s -> !s.isBlank())
              .toList();

      docToQuestionKeys.put(docId, allKeys);
    }

    // 5. 랜덤 선택
    List<String> selectedKeys = new ArrayList<>();
    List<String> leftoverKeys = new ArrayList<>();

    for (Map.Entry<Integer, Integer> entry : questionCountPerDoc.entrySet()) {
      Integer docId = entry.getKey();
      int count = entry.getValue();

      List<String> available = new ArrayList<>(docToQuestionKeys.getOrDefault(docId, List.of()));
      Collections.shuffle(available);

      if (available.size() >= count) {
        selectedKeys.addAll(available.subList(0, count));
      } else {
        selectedKeys.addAll(available);
        log.warn("📉 문서 {} 에서 부족한 문제 수: {}", docId, count - available.size());
      }

      leftoverKeys.addAll(available);
    }

    // 6. 부족한 수량 보정 (다른 문서에서)
    int remaining = totalCount - selectedKeys.size();
    if (remaining > 0) {
      Collections.shuffle(leftoverKeys);
      for (String key : leftoverKeys) {
        if (!selectedKeys.contains(key)) {
          selectedKeys.add(key);
          if (selectedKeys.size() == totalCount) break;
        }
      }
    }

    // 7. MongoDB에서 조회
    List<Question> selectedQuestions = questionMongoRepository.findAllById(selectedKeys);

    log.info("🔍 요청 문제 수: {}, 실제 조회 수: {}", totalCount, selectedQuestions.size());
    return selectedQuestions;
  }

  /**
   * 프로젝트 ID에 따른 문서의 문제 수를 조회합니다.
   *
   * @param projectId 프로젝트 ID
   * @return 문서별 문제 수 리스트
   */
  public List<DocumentQuestionCountDto> getDocumentQuestionCountsByProject(Integer projectId) {
    // 1. 해당 프로젝트의 문서 목록
    List<Document> documents = documentRepository.findAllByProject_ProjectId(projectId);
    List<Integer> documentIds = documents.stream().map(Document::getDocumentId).toList();

    // 2. 문서 ID → 문서 객체 매핑
    Map<Integer, Document> documentMap =
        documents.stream().collect(Collectors.toMap(Document::getDocumentId, doc -> doc));

    // 3. 해당 문서들에 대한 DocumentQuestion 전체 조회
    List<DocumentQuestion> allDocumentQuestions =
        documentQuestionRepository.findByDocument_DocumentIdIn(documentIds);

    // 4. 문서별로 DocumentQuestion 합산
    Map<Integer, List<DocumentQuestion>> grouped =
        allDocumentQuestions.stream()
            .collect(Collectors.groupingBy(dq -> dq.getDocument().getDocumentId()));

    // 5. 최종 결과 구성
    return grouped.entrySet().stream()
        .map(
            entry -> {
              Integer documentId = entry.getKey();
              List<DocumentQuestion> dqList = entry.getValue();

              int objTotal =
                  dqList.stream().mapToInt(DocumentQuestion::getConfiguredObjectiveCount).sum();
              int subTotal =
                  dqList.stream().mapToInt(DocumentQuestion::getConfiguredSubjectiveCount).sum();

              Document doc = documentMap.get(documentId);
              return new DocumentQuestionCountDto(documentId, doc.getName(), objTotal + subTotal);
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

    return inviteLink.getToken();
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
   * 유저 ID와 테스트 ID로 특정 테스트를 조회합니다.
   *
   * @param userId 유저 ID
   * @param testId 테스트 ID
   * @return ResponseTestDto (문제 리스트 포함, 일부 필드 번역 적용)
   */
  public ResponseTestDto getTestByUserTestId(Integer userId, Integer testId, String lang) {
    log.info("Fetching test with ID: {}, userId: {}", testId, userId);

    UserTest userTest =
        userTestRepository
            .findByUser_UserIdAndTest_TestIdAndIsDeletedFalse(userId, testId)
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "해당 유저테스트를 찾을 수 없습니다: userId=" + userId + ", testId=" + testId));

    if (Boolean.TRUE.equals(userTest.getRetake())) {
      throw new IllegalStateException("해당 테스트는 재응시 상태입니다. 접근할 수 없습니다.");
    }

    return buildTestDtoWithQuestions(userTest.getTest(), lang);
  }

  /**
   * 테스트 ID로 테스트를 조회하고, 문제 리스트를 포함한 DTO를 반환합니다.
   *
   * @param testId 테스트 ID
   * @param lang 언어 코드 (예: "ko", "en")
   * @return ResponseTestDto (문제 리스트 포함, 일부 필드 번역 적용)
   */
  public ResponseTestDto getTestById(Integer testId, String lang) {
    log.info("Fetching test with ID: {}", testId);

    Test test =
        testRepository
            .findById(testId)
            .orElseThrow(() -> new IllegalArgumentException("해당 테스트를 찾을 수 없습니다: " + testId));

    return buildTestDtoWithQuestions(test, lang);
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

    // 연관된 UserTest 논리 삭제
    List<UserTest> userTests = userTestRepository.findAllByTest(test);
    userTests.forEach(u -> u.setIsDeleted(true));
    userTestRepository.saveAll(userTests);

    // 연관된 InviteLink 논리 삭제
    List<InviteLink> inviteLinks = inviteLinkRepository.findAllByTest(test);
    inviteLinks.forEach(i -> i.setIsDeleted(true));
    inviteLinkRepository.saveAll(inviteLinks);

    // 연관된 TestDocumentConfig 논리 삭제
    List<TestDocumentConfig> configs = testDocumentConfigRepository.findAllByTest(test);
    configs.forEach(c -> c.setIsDeleted(true));
    testDocumentConfigRepository.saveAll(configs);

    // 연관된 TestQuestion 논리 삭제
    List<TestQuestion> questions = testQuestionRepository.findAllByTest(test);
    questions.forEach(q -> q.setIsDeleted(true));
    testQuestionRepository.saveAll(questions);

    // 연관된 userAnswers 논리 삭제
    for (UserTest userTest : userTests) {
      answerService.deleteAnswersByUserTest(userTest);
    }

    // Test 자체 논리 삭제
    test.setIsDeleted(true);
    testRepository.save(test);

    log.info("Test and all related entities deleted successfully: {}", test.getName());
  }

  /**
   * 초대 토큰으로 유저를 테스트에 등록하고, 테스트 정보를 반환합니다.
   *
   * @param token 초대 토큰
   * @param userId 유저 ID
   * @param lang 언어 코드 (예: "ko", "en")
   * @return ResponseTestDto (문제 리스트 포함)
   */
  public ResponseTestDto registerUserToTest(String token, Integer userId, String lang) {
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
    return buildTestDtoWithQuestions(test, lang);
  }

  /**
   * 테스트 ID로 연결된 문제 리스트를 조회합니다.
   *
   * @param testId 테스트 ID
   * @return 문제 리스트
   */
  @Transactional(readOnly = true)
  public List<QuestionDto> getQuestionsByTestId(Integer testId) {
    Test test =
        testRepository
            .findById(testId)
            .orElseThrow(() -> new IllegalArgumentException("Test not found: " + testId));

    String questionIdCsv = test.getQuestionIds(); // 예: "id1,id2,id3"
    if (questionIdCsv == null || questionIdCsv.isBlank()) {
      return List.of();
    }

    List<String> questionIds = Arrays.asList(questionIdCsv.split(","));
    List<Question> questions = questionMongoRepository.findAllById(questionIds);

    return questions.stream().map(questionToDtoConverter::convert).collect(Collectors.toList());
  }

  private ResponseTestDto buildTestDtoWithQuestions(Test test, String lang) {
    // 1. TestQuestion → questionId 수집
    List<TestQuestion> testQuestions = testQuestionRepository.findByTest(test);
    List<String> questionIds =
        testQuestions.stream().map(TestQuestion::getQuestionId).collect(Collectors.toList());

    // 2. MongoDB에서 실제 문제 조회
    List<Question> questions = questionMongoRepository.findAllById(questionIds);

    // 3. Question → QuestionDto 변환 + 번역
    List<QuestionDto> questionDtos =
        questions.stream()
            .map(questionDtoConverter::convert)
            .map(
                q ->
                    !"ko".equalsIgnoreCase(lang)
                        ? questionTranslator.translateQuestionDto(q, lang)
                        : q)
            .collect(Collectors.toList());

    // 4. Test → ResponseTestDto 변환
    ResponseTestDto responseDto = testDtoConverter.convert(test);
    responseDto.setQuestions(questionDtos);
    responseDto.setPassScore(test.getPassScore());

    return responseDto;
  }

  public void deleteTestsByProjectId(Integer projectId) {
    log.info("Deleting all tests for project ID: {}", projectId);

    // 1. 프로젝트에 연결된 테스트들 조회
    List<Test> tests = testRepository.findAllByProject_ProjectIdAndIsDeletedFalse(projectId);
    if (tests.isEmpty()) {
      log.warn("해당 프로젝트에 연결된 테스트가 없습니다. projectId={}", projectId);
      return;
    }

    // 2. 각 테스트 삭제 (기존 deleteTest 재사용)
    for (Test test : tests) {
      log.info("Deleting test via service: {} - {}", test.getTestId(), test.getName());
      this.deleteTest(test.getTestId()); // ✅ 핵심 변경
    }

    log.info("✅ Project ID {}에 연결된 모든 테스트가 삭제되었습니다.", projectId);
  }
}

package com.t1f5.skib.question.service;

import com.t1f5.skib.global.enums.TestStatus;
import com.t1f5.skib.global.services.TranslationService;
import com.t1f5.skib.question.domain.Question;
import com.t1f5.skib.question.dto.QuestionDto;
import com.t1f5.skib.question.dto.QuestionDtoConverter;
import com.t1f5.skib.question.dto.QuestionValueDto;
import com.t1f5.skib.question.dto.TestResultDto;
import com.t1f5.skib.question.repository.QuestionMongoRepository;
import com.t1f5.skib.test.domain.Test;
import com.t1f5.skib.test.dto.RequestCreateTestDto;
import com.t1f5.skib.test.repository.TestRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@Slf4j
@RequiredArgsConstructor
public class QuestionService {
  private final WebClient webClient;
  private final QuestionMongoRepository questionMongoRepository;
  private final TestRepository testRepository;
  private final QuestionDtoConverter questionDtoConverter;
  private final MongoTemplate mongoTemplate;
  private final TranslationService translationService;

  @Value("${fastapi.base-url}")
  private String fastApiBaseUrl;

  public void sendTestCreationRequest(RequestCreateTestDto dto) {
    try {
      webClient
          .post()
          .uri(fastApiBaseUrl + "/api/test/generate")
          .contentType(MediaType.APPLICATION_JSON)
          .bodyValue(dto)
          .retrieve()
          .toBodilessEntity()
          .block();

      log.info("✅ FastAPI에 문제 생성 요청 완료: testId={}", dto.getTestId());
    } catch (Exception e) {
      log.error("❌ FastAPI 문제 생성 요청 실패: {}", e.getMessage(), e);
    }
  }

  @Transactional
  public void saveGeneratedQuestions(TestResultDto dto) {
    // 1. QuestionDto → Mongo Entity 변환
    List<Question> questions =
        dto.getQuestions().stream().map(questionDtoConverter::convert).toList();

    // Question에 대한 로그 찍기
    questions.forEach(question -> log.info("✅ 생성된 문제: {}", question));

    // 2. MongoDB에 저장
    questionMongoRepository.saveAll(questions);

    // 3. 저장된 questionId들 추출
    String questionIds =
        questions.stream()
            .map(Question::getId) // Mongo 엔티티의 @Id
            .collect(Collectors.joining(","));

    // 4. Test 상태 갱신
    Test test =
        testRepository
            .findById(dto.getTestId())
            .orElseThrow(() -> new IllegalArgumentException("Test not found: " + dto.getTestId()));
    test.setQuestionIds(questionIds);
    test.setStatus(TestStatus.COMPLETED);
    testRepository.save(test);

    log.info("✅ 문제 저장 완료: testId={}, questions={}", dto.getTestId(), questionIds);
  }

  /*
   * 문제를 수정하는 메서드
   * 이 메서드는 MongoDB에서 문제를 찾아 업데이트합니다.
   * @param key 문제의 고유 키
   * @param updatedValueDto 수정할 문제의 값 DTO
   */
  public void updateQuestion(String key, QuestionValueDto updatedValueDto) {
    Query query = new Query(Criteria.where("key").is(key));

    Update update = new Update();

    if (updatedValueDto.getQuestion() != null) {
      update.set("value.question", updatedValueDto.getQuestion());
    }
    if (updatedValueDto.getOptions() != null) {
      update.set("value.options", updatedValueDto.getOptions());
    }
    if (updatedValueDto.getAnswer() != null) {
      update.set("value.answer", updatedValueDto.getAnswer());
    }
    if (updatedValueDto.getGrading_criteria() != null) {
      update.set("value.grading_criteria", updatedValueDto.getGrading_criteria());
    }
    if (updatedValueDto.getExplanation() != null) {
      update.set("value.explanation", updatedValueDto.getExplanation());
    }

    mongoTemplate.updateFirst(query, update, "QUESTION");
  }

  /**
   * 주어진 원본 문제(QuestionDto)를 기반으로, 지정된 언어로 번역된 새 QuestionDto를 생성합니다.
   *
   * @param original 원본 QuestionDto (기존 한국어 기반 문제)
   * @param targetLang 대상 언어 코드 (예: "en", "ja", "zh" 등)
   * @return 번역된 QuestionDto 객체
   */
  public QuestionDto translateQuestionDto(QuestionDto original, String targetLang) {
    return QuestionDto.builder()
        .type(original.getType())
        .difficulty_level(original.getDifficulty_level())
        .question(translateText(original.getQuestion(), targetLang)) // 번역
        .options(
            original.getOptions() != null
                ? original.getOptions().stream()
                    .map(opt -> translateText(opt, targetLang))
                    .collect(Collectors.toList())
                : null)
        .answer(original.getAnswer())
        .explanation(original.getExplanation())
        .grading_criteria(original.getGrading_criteria())
        .documentId(original.getDocumentId())
        .tags(original.getTags())
        .build();
  }

  /**
   * 주어진 텍스트를 지정된 언어로 번역합니다.
   *
   * <p>예외 처리: - 텍스트가 null이거나 비어있거나(""), 대상 언어가 한국어("ko")일 경우 번역하지 않고 원문 반환
   *
   * @param text 번역할 원본 문자열
   * @param targetLang 대상 언어 코드
   * @return 번역된 문자열 (혹은 원본 텍스트)
   */
  private String translateText(String text, String targetLang) {
    if (text == null || text.isBlank() || "ko".equalsIgnoreCase(targetLang)) {
      return text;
    }
    return translationService.translate(text, targetLang);
  }
}

package com.t1f5.skib.question.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.t1f5.skib.global.services.TranslationService;
import com.t1f5.skib.question.domain.Question;
import com.t1f5.skib.question.dto.QuestionDto;
import com.t1f5.skib.question.dto.QuestionDtoConverter;
import com.t1f5.skib.question.dto.QuestionResponse;
import com.t1f5.skib.question.dto.QuestionValueDto;
import com.t1f5.skib.question.repository.QuestionMongoRepository;
import com.t1f5.skib.test.dto.RequestCreateTestDto;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@Slf4j
@RequiredArgsConstructor
public class QuestionService {
  private final WebClient webClient;
  private final QuestionMongoRepository questionMongoRepository;
  private final QuestionDtoConverter questionDtoConverter;
  private final MongoTemplate mongoTemplate;
  private final TranslationService translationService;

  @Value("${fastapi.base-url}")
  private String fastApiBaseUrl;

  /**
   * LLM을 사용하여 문제를 생성하는 메서드
   *
   * @param requestDto 문제 생성 요청 DTO
   * @return 생성된 문제 목록
   */
  public List<Question> generateQuestions(RequestCreateTestDto requestDto) {
    QuestionResponse response =
        webClient
            .post()
            .uri(fastApiBaseUrl + "api/test/generate")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .bodyValue(requestDto)
            .retrieve()
            .bodyToMono(QuestionResponse.class)
            .block(); // 동기식 호출 (RestTemplate과 동일하게 처리)

    ObjectMapper mapper = new ObjectMapper();

    try {
        log.info("🧪 RAW FastAPI Response: {}", mapper.writeValueAsString(response));
        log.info("🧪 Deserialized DTO (1st Question): {}", mapper.writeValueAsString(response.getQuestions().get(0)));
        log.info("🧪 grading_criteria: {}", mapper.writeValueAsString(response.getQuestions().get(0).getGrading_criteria()));
    } catch (Exception e) {
        log.error("🛑 JSON 직렬화 중 오류 발생", e);
    }


    if (response == null || response.getQuestions() == null) return List.of();

    List<Question> questions =
        response.getQuestions().stream().map(questionDtoConverter::convert).toList();

    questionMongoRepository.saveAll(questions);
    return questions;
  }

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

  private String translateText(String text, String targetLang) {
    if (text == null || text.isBlank() || "ko".equalsIgnoreCase(targetLang)) {
      return text;
    }
    return translationService.translate(text, targetLang);
  }
}

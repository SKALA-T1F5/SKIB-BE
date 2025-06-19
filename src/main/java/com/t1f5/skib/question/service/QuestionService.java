package com.t1f5.skib.question.service;

import com.t1f5.skib.global.services.TranslationService;
import com.t1f5.skib.question.domain.Question;
import com.t1f5.skib.question.dto.QuestionDto;
import com.t1f5.skib.question.dto.QuestionDtoConverter;
import com.t1f5.skib.question.dto.QuestionValueDto;
import com.t1f5.skib.question.dto.RequestCreateQuestionDto;
import com.t1f5.skib.question.repository.QuestionMongoRepository;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
@RequiredArgsConstructor
public class QuestionService {

  private final RestTemplate restTemplate;
  private final QuestionMongoRepository questionMongoRepository;
  private final QuestionDtoConverter questionDtoConverter;
  private final MongoTemplate mongoTemplate;
  private final TranslationService translationService;

  public List<Question> generateQuestions(List<RequestCreateQuestionDto> requests) {
    List<Question> allQuestions = new ArrayList<>();

    for (RequestCreateQuestionDto request : requests) {
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      HttpEntity<RequestCreateQuestionDto> entity = new HttpEntity<>(request, headers);

      ResponseEntity<QuestionDto[]> response =
          restTemplate.postForEntity(
              "http://10.250.73.244:8000/api/question", entity, QuestionDto[].class);

      QuestionDto[] body = response.getBody();
      if (body == null) continue;

      List<Question> questions =
          Arrays.stream(body).map(dto -> questionDtoConverter.convert(dto)).toList();

      questionMongoRepository.saveAll(questions);
      allQuestions.addAll(questions);
    }

    return allQuestions;
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
        .document_id(original.getDocument_id())
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

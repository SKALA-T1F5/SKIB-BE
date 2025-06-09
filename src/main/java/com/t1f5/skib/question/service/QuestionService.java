package com.t1f5.skib.question.service;

import com.t1f5.skib.question.domain.Question;
import com.t1f5.skib.question.dto.DocumentQuestionRequest;
import com.t1f5.skib.question.dto.QuestionDto;
import com.t1f5.skib.question.dto.QuestionDtoConverter;
import com.t1f5.skib.question.dto.QuestionValueDto;
import com.t1f5.skib.question.repository.QuestionMongoRepository;
import java.util.Arrays;
import java.util.List;
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

  public void generateQuestions(List<DocumentQuestionRequest> requests, Integer projectId) {
    String url = "http://10.250.73.103:8000/api/question";
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    for (DocumentQuestionRequest request : requests) {
      HttpEntity<DocumentQuestionRequest> entity = new HttpEntity<>(request, headers);

      ResponseEntity<QuestionDto[]> response =
          restTemplate.postForEntity(url, entity, QuestionDto[].class);

      List<Question> questions =
          Arrays.stream(response.getBody())
              .map(dto -> questionDtoConverter.convert(dto, projectId))
              .toList();

      questionMongoRepository.saveAll(questions);
    }
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
}

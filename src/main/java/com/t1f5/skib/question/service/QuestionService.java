package com.t1f5.skib.question.service;

import com.t1f5.skib.question.domain.Question;
import com.t1f5.skib.question.dto.DocumentQuestionRequest;
import com.t1f5.skib.question.dto.QuestionDto;
import com.t1f5.skib.question.dto.QuestionDtoConverter;
import com.t1f5.skib.question.repository.QuestionMongoRepository;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

  public void generateQuestions(List<DocumentQuestionRequest> requests) {
    String url = "http://10.250.73.103:8000/api/question";
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    for (DocumentQuestionRequest request : requests) {
      HttpEntity<DocumentQuestionRequest> entity = new HttpEntity<>(request, headers);

      ResponseEntity<QuestionDto[]> response =
          restTemplate.postForEntity(url, entity, QuestionDto[].class);

      List<Question> questions =
          Arrays.stream(response.getBody()).map(questionDtoConverter::convert).toList();

      questionMongoRepository.saveAll(questions);
    }
  }
}

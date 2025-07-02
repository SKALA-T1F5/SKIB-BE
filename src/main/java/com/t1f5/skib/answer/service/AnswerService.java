package com.t1f5.skib.answer.service;

import com.t1f5.skib.answer.domain.Answer;
import com.t1f5.skib.answer.domain.SubjectiveAnswer;
import com.t1f5.skib.answer.dto.AnswerRequest;
import com.t1f5.skib.answer.dto.RequestCreateAnswerDto;
import com.t1f5.skib.answer.dto.ResponseSubjectiveAnswerDto;
import com.t1f5.skib.answer.dto.ScoredAnswerResultDto;
import com.t1f5.skib.answer.dto.SubjectiveAnswerDtoConverter;
import com.t1f5.skib.answer.dto.SubjectiveScoringRequestDto;
import com.t1f5.skib.answer.dto.SubjectiveScoringResponseDto;
import com.t1f5.skib.answer.repository.AnswerRepository;
import com.t1f5.skib.answer.repository.SubjectiveAnswerRepository;
import com.t1f5.skib.global.enums.QuestionType;
import com.t1f5.skib.question.domain.Question;
import com.t1f5.skib.question.dto.GradingCriteriaDto;
import com.t1f5.skib.question.dto.QuestionDto;
import com.t1f5.skib.question.dto.QuestionToDtoConverter;
import com.t1f5.skib.question.repository.QuestionMongoRepository;
import com.t1f5.skib.test.domain.UserTest;
import com.t1f5.skib.test.dto.QuestionTranslator;
import com.t1f5.skib.test.repository.UserTestRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Slf4j
@Service
public class AnswerService {
  private final AnswerRepository answerRepository;
  private final SubjectiveAnswerRepository subjectiveAnswerRepository;
  private final UserTestRepository userTestRepository;
  private final QuestionMongoRepository questionMongoRepository;
  private final WebClient webClient;
  private final SubjectiveAnswerDtoConverter subjectiveAnswerDtoConverter;
  private final QuestionToDtoConverter questionToDtoConverter;
  @Autowired private QuestionTranslator questionTranslator;

  @Value("${fastapi.base-url}")
  private String fastApiBaseUrl;

  /**
   * 사용자가 제출한 답변을 저장합니다.
   *
   * @param dto 사용자가 제출한 답변 DTO
   * @param userTestId 사용자 테스트 ID
   */
  public void saveAnswer(RequestCreateAnswerDto dto, Integer userId, Integer testId) {
    UserTest userTest =
        userTestRepository
            .findByUser_UserIdAndTest_TestIdAndIsDeletedFalse(userId, testId)
            .orElseThrow(() -> new IllegalArgumentException("해당 유저의 테스트가 존재하지 않습니다."));

    for (AnswerRequest item : dto.getAnswers()) {
      Boolean isCorrect = null;
      int score = 0;

      if (item.getQuestionType() == QuestionType.OBJECTIVE) {
        isCorrect = getIsCorrectForMultipleChoice(item.getId(), item.getResponse());
      } else if (item.getQuestionType() == QuestionType.SUBJECTIVE) {
        Question question =
            questionMongoRepository
                .findById(item.getId())
                .orElseThrow(() -> new IllegalArgumentException("해당 주관식 문제를 찾을 수 없습니다."));

        List<GradingCriteriaDto> gradingCriteria = question.getGradingCriteria();

        SubjectiveScoringResponseDto response =
            scoreSubjectiveAnswer(item.getId(), gradingCriteria, item.getResponse());
        score = response.getScore();
      }

      Answer answer =
          Answer.builder()
              .userTest(userTest)
              .questionId(item.getId())
              .response(item.getResponse())
              .isCorrect(isCorrect)
              .type(item.getQuestionType())
              .isDeleted(false)
              .build();

      Answer saved = answerRepository.save(answer);

      if (item.getQuestionType() == QuestionType.SUBJECTIVE) {
        SubjectiveAnswer subjectiveAnswer =
            SubjectiveAnswer.builder()
                .userAnswerId(String.valueOf(saved.getUserAnswerId()))
                .questionId(item.getId())
                .score(score)
                .build();

        subjectiveAnswerRepository.save(subjectiveAnswer);

        ResponseSubjectiveAnswerDto dtoResult =
            subjectiveAnswerDtoConverter.convert(subjectiveAnswer);
        log.info("주관식 DTO 변환 결과: {}", dtoResult);
      }
    }

    userTest.setIsTaken(true);
    userTestRepository.save(userTest);
  }

  /**
   * 사용자가 제출한 답변을 조회합니다.
   *
   * @param userId 사용자 ID
   * @param testId 테스트 ID
   * @return 사용자가 제출한 답변 목록
   */
  public List<ScoredAnswerResultDto> getScoredAnswersByUserTestId(
      Integer userId, Integer testId, String lang) {

    List<Answer> answers = answerRepository.findByUserIdAndTestId(userId, testId);

    List<ScoredAnswerResultDto> results = new ArrayList<>();

    for (Answer answer : answers) {
      String questionId = answer.getQuestionId();

      Question question =
          questionMongoRepository
              .findById(questionId)
              .orElseThrow(() -> new IllegalArgumentException("해당 문제를 찾을 수 없습니다: " + questionId));

      // 번역 적용
      QuestionDto questionDto = questionToDtoConverter.convert(question);
      if (!"ko".equalsIgnoreCase(lang)) {
        questionDto = questionTranslator.translateQuestionDto(questionDto, lang);
      }

      SubjectiveAnswer subjectiveAnswer = null;
      Integer score = 0;

      if (answer.getType() == QuestionType.SUBJECTIVE) {
        subjectiveAnswer =
            subjectiveAnswerRepository
                .findByUserAnswerId(String.valueOf(answer.getUserAnswerId()))
                .orElse(null);

        if (subjectiveAnswer != null) {
          score = subjectiveAnswer.getScore();
        }
      }

      ScoredAnswerResultDto dto =
          ScoredAnswerResultDto.builder()
              .questionId(answer.getQuestionId())
              .type(answer.getType())
              .question(question.getQuestion())
              .options(question.getOptions())
              .explanation(question.getExplanation())
              .response(answer.getResponse())
              .answer(question.getAnswer())
              .isCorrect(Boolean.TRUE.equals(answer.getIsCorrect()))
              .score(score)
              .build();

      results.add(dto);
    }

    return results;
  }

  private Boolean getIsCorrectForMultipleChoice(String questionId, String userResponse) {
    Question question =
        questionMongoRepository
            .findById(questionId)
            .orElseThrow(() -> new IllegalArgumentException("해당 문제를 찾을 수 없습니다."));

    String correctAnswer = question.getAnswer();
    return checkCorrectAnswer(correctAnswer, userResponse);
  }

  private boolean checkCorrectAnswer(String correct, String user) {
    if (Objects.isNull(correct) || Objects.isNull(user)) return false;
    return correct.trim().equalsIgnoreCase(user.trim());
  }

  private SubjectiveScoringResponseDto scoreSubjectiveAnswer(
      String questionId, List<GradingCriteriaDto> grading_criteria, String response) {
    return webClient
        .post()
        .uri(fastApiBaseUrl + "/api/grading/subjective")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(new SubjectiveScoringRequestDto(questionId, grading_criteria, response))
        .retrieve()
        .onStatus(
            status -> status.is4xxClientError() || status.is5xxServerError(),
            clientResponse ->
                clientResponse
                    .bodyToMono(String.class)
                    .flatMap(
                        error -> {
                          log.error("FastAPI 채점 오류 응답: {}", error);
                          return Mono.error(new RuntimeException("FastAPI 채점 실패: " + error));
                        }))
        .bodyToMono(SubjectiveScoringResponseDto.class)
        .block();
  }

  /**
   * 유저테스트 엔티티로 해당 유저테스트의 답변을 삭제(soft delete)합니다.
   *
   * @param userTest 유저테스트 엔티티
   */
  public void deleteAnswersByUserTest(UserTest userTest) {
    // 1. 해당 유저테스트의 답변 전체 조회
    List<Answer> answers = answerRepository.findByUserTest(userTest);

    for (Answer answer : answers) {
      // 2. 답변 soft delete 처리
      answer.setIsDeleted(true);
      answerRepository.save(answer);

      // 3. 주관식 채점 결과도 삭제 (있는 경우에만)
      if (answer.getType() == QuestionType.SUBJECTIVE) {
        SubjectiveAnswer subjectiveAnswer =
            subjectiveAnswerRepository
                .findByUserAnswerId(String.valueOf(answer.getUserAnswerId()))
                .orElse(null);

        if (subjectiveAnswer != null) {
          subjectiveAnswerRepository.delete(subjectiveAnswer);
          log.info("주관식 채점 결과 삭제됨: {}", subjectiveAnswer.getId());
        }
      }
      log.info("답변 삭제 완료: userAnswerId={}", answer.getUserAnswerId());
    }
  }
}

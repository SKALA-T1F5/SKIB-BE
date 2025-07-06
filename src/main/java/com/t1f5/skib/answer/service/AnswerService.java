package com.t1f5.skib.answer.service;

import com.t1f5.skib.answer.domain.Answer;
import com.t1f5.skib.answer.domain.SubjectiveAnswer;
import com.t1f5.skib.answer.dto.AnswerRequest;
import com.t1f5.skib.answer.dto.RequestCreateAnswerDto;
import com.t1f5.skib.answer.dto.ScoredAnswerResultDto;
import com.t1f5.skib.answer.dto.SubjectiveAnswerDtoConverter;
import com.t1f5.skib.answer.dto.SubjectiveScoringRequestDto;
import com.t1f5.skib.answer.dto.SubjectiveScoringResponseDto;
import com.t1f5.skib.answer.repository.AnswerRepository;
import com.t1f5.skib.answer.repository.SubjectiveAnswerRepository;
import com.t1f5.skib.global.enums.AttemptType;
import com.t1f5.skib.global.enums.QuestionType;
import com.t1f5.skib.question.domain.Question;
import com.t1f5.skib.question.dto.GradingCriteriaDto;
import com.t1f5.skib.question.dto.QuestionDto;
import com.t1f5.skib.question.dto.QuestionToDtoConverter;
import com.t1f5.skib.question.repository.QuestionMongoRepository;
import com.t1f5.skib.test.domain.Test;
import com.t1f5.skib.test.domain.UserTest;
import com.t1f5.skib.test.dto.QuestionTranslator;
import com.t1f5.skib.test.repository.UserTestRepository;
import com.t1f5.skib.user.model.User;
import java.time.LocalDateTime;
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

  public void saveAnswer(RequestCreateAnswerDto dto, Integer userId, Integer testId) {
    UserTest userTest =
        userTestRepository
            .findByUser_UserIdAndTest_TestIdAndIsDeletedFalse(userId, testId)
            .orElse(null);

    if (userTest == null) {
      // 안전장치: 혹시 register 없이 직접 접근했을 경우
      userTest =
          UserTest.builder()
              .user(User.builder().userId(userId).build())
              .test(Test.builder().testId(testId).build())
              .isTaken(true)
              .isPassed(false)
              .retake(false)
              .takenDate(LocalDateTime.now())
              .score(0)
              .isDeleted(false)
              .build();
      userTestRepository.save(userTest);
      saveAnswersByAttempt(dto, userTest, AttemptType.FIRST);
      return;
    }

    boolean hasFirstAttempt =
        answerRepository.existsByUserTestAndAttemptType(userTest, AttemptType.FIRST);

    if (!hasFirstAttempt) {
      // 첫 응시
      userTest.setIsTaken(true);
      saveAnswersByAttempt(dto, userTest, AttemptType.FIRST);
    } else if (!userTest.getRetake()) {
      // 재응시 전 첫 응시 끝난 상태
      Test test = userTest.getTest();
      if (test == null || !Boolean.TRUE.equals(test.getIsRetake())) {
        throw new IllegalStateException("이 테스트는 재응시가 허용되지 않습니다.");
      }

      userTest.setRetake(true);
      userTest.setIsTaken(true);
      saveAnswersByAttempt(dto, userTest, AttemptType.RETRY);
    } else {
      throw new IllegalStateException("이미 재응시까지 완료된 시험입니다.");
    }

    userTestRepository.save(userTest);
  }

  private void saveAnswersByAttempt(
      RequestCreateAnswerDto dto, UserTest userTest, AttemptType attemptType) {
    int totalScore = 0;
    int pointPerQuestion = 100 / dto.getAnswers().size();

    for (AnswerRequest item : dto.getAnswers()) {
      boolean exists =
          answerRepository.existsByUserTestAndQuestionIdAndAttemptType(
              userTest, item.getId(), attemptType);

      if (exists) {
        log.warn(
            "❗ 이미 저장된 답변입니다: questionId={}, userTestId={}, attemptType={}",
            item.getId(),
            userTest.getUserTestId(),
            attemptType);
        continue;
      }

      int score = handleAnswer(item, userTest, pointPerQuestion, attemptType);
      totalScore += score;
    }

    userTest.setScore(totalScore);

    Test test = userTest.getTest();
    if (test != null && test.getPassScore() != null) {
      boolean isPassed = totalScore >= test.getPassScore();
      userTest.setIsPassed(isPassed);
    }
  }

  private int handleAnswer(
      AnswerRequest item, UserTest userTest, int pointPerQuestion, AttemptType attemptType) {
    // 한 번만 조회
    Question question =
        questionMongoRepository
            .findById(item.getId())
            .orElseThrow(() -> new IllegalArgumentException("해당 문제를 찾을 수 없습니다."));
    QuestionType type = question.getType();

    Boolean isCorrect = null;
    int score = 0;

    if (type == QuestionType.OBJECTIVE) {
      isCorrect = checkCorrectAnswer(question.getAnswer(), item.getResponse());
      score = Boolean.TRUE.equals(isCorrect) ? pointPerQuestion : 0;

    } else if (type == QuestionType.SUBJECTIVE) {
      List<GradingCriteriaDto> gradingCriteria = question.getGradingCriteria();
      SubjectiveScoringResponseDto response =
          scoreSubjectiveAnswer(item.getId(), gradingCriteria, item.getResponse());
      score = response.getScore();
      isCorrect = score >= 5;
    } else {
      throw new IllegalStateException("지원하지 않는 문제 유형입니다.");
    }

    // documentId 변환
    Integer docId = null;
    try {
      if (question.getDocumentId() != null && !question.getDocumentId().isBlank()) {
        docId = Integer.valueOf(question.getDocumentId());
      }
    } catch (NumberFormatException e) {
      log.warn("문서 ID 파싱 실패: {}", question.getDocumentId(), e);
    }

    Answer answer =
        Answer.builder()
            .userTest(userTest)
            .questionId(item.getId())
            .response(item.getResponse())
            .isCorrect(isCorrect)
            .score(score)
            .type(type) // DB 기준 사용
            .documentId(docId)
            .documentName(question.getDocumentName())
            .attemptType(attemptType)
            .isDeleted(false)
            .build();

    Answer saved = answerRepository.save(answer);

    if (type == QuestionType.SUBJECTIVE) {
      SubjectiveAnswer subjectiveAnswer =
          SubjectiveAnswer.builder()
              .userAnswerId(String.valueOf(saved.getUserAnswerId()))
              .questionId(item.getId())
              .score(score)
              .build();
      subjectiveAnswerRepository.save(subjectiveAnswer);
      log.info("주관식 DTO 변환 결과: {}", subjectiveAnswerDtoConverter.convert(subjectiveAnswer));
    }

    return score;
  }

  /**
   * 특정 유저의 테스트에 대한 채점된 답변 결과를 반환합니다.
   *
   * @param userId 유저 ID
   * @param testId 테스트 ID
   * @param lang 언어 코드 (예: "ko", "en")
   * @return 채점된 답변 결과 리스트
   */
  public List<ScoredAnswerResultDto> getScoredAnswersByUserTestId(
      Integer userId, Integer testId, String lang, AttemptType attemptType) {
    UserTest userTest =
        userTestRepository
            .findByUser_UserIdAndTest_TestIdAndIsDeletedFalse(userId, testId)
            .orElseThrow(() -> new IllegalArgumentException("해당 유저의 테스트가 존재하지 않습니다."));

    List<Answer> answers =
        answerRepository.findByUserTest_UserTestIdAndAttemptType(
            userTest.getUserTestId(), attemptType);
    List<ScoredAnswerResultDto> results = new ArrayList<>();

    for (Answer answer : answers) {
      log.info("🔍 Answer에서 꺼낸 questionId = {}", answer.getQuestionId());
      String questionId = answer.getQuestionId();

      Question question =
          questionMongoRepository
              .findById(questionId)
              .orElseThrow(() -> new IllegalArgumentException("해당 문제를 찾을 수 없습니다: " + questionId));

      QuestionDto questionDto = questionToDtoConverter.convert(question);
      if (!"ko".equalsIgnoreCase(lang)) {
        questionDto = questionTranslator.translateAllQuestionDto(questionDto, lang);
      }

      SubjectiveAnswer subjectiveAnswer = null;
      Integer score = answer.getScore();

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
              .question(questionDto.getQuestion())
              .options(questionDto.getOptions())
              .explanation(questionDto.getExplanation())
              .response(answer.getResponse())
              .answer(questionDto.getAnswer())
              .isCorrect(Boolean.TRUE.equals(answer.getIsCorrect()))
              .score(score)
              .build();

      results.add(dto);
    }

    return results;
  }

  /**
   * 특정 유저의 테스트에 대한 답변을 삭제합니다. 주관식 문제의 경우 채점 결과도 함께 삭제됩니다.
   *
   * @param userTest 유저 테스트 정보
   */
  public void deleteAnswersByUserTest(UserTest userTest) {
    List<Answer> answers = answerRepository.findByUserTest(userTest);

    for (Answer answer : answers) {
      answer.setIsDeleted(true);
      answerRepository.save(answer);

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

  private boolean checkCorrectAnswer(String correct, String user) {
    if (Objects.isNull(correct) || Objects.isNull(user)) return false;
    return correct.trim().equalsIgnoreCase(user.trim());
  }

  private SubjectiveScoringResponseDto scoreSubjectiveAnswer(
      String questionId, List<GradingCriteriaDto> gradingCriteria, String response) {
    return webClient
        .post()
        .uri(fastApiBaseUrl + "/api/grading/subjective")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(new SubjectiveScoringRequestDto(questionId, gradingCriteria, response))
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
}
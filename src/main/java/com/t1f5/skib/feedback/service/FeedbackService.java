package com.t1f5.skib.feedback.service;

import com.t1f5.skib.answer.domain.Answer;
import com.t1f5.skib.answer.repository.AnswerRepository;
import com.t1f5.skib.answer.repository.QuestionCorrectRateProjection;
import com.t1f5.skib.document.domain.Document;
import com.t1f5.skib.feedback.dto.RequestFeedbackForLLMDto;
import com.t1f5.skib.feedback.dto.ResponseAnswerMatrixDto;
import com.t1f5.skib.feedback.dto.ResponseAnswerMatrixDto.AnswerRow;
import com.t1f5.skib.feedback.dto.ResponseFeedbackAllDto;
import com.t1f5.skib.feedback.dto.ResponseFeedbackDistributionDto;
import com.t1f5.skib.feedback.dto.ResponseFeedbackDocDto;
import com.t1f5.skib.feedback.dto.ResponseFeedbackTagDto;
import com.t1f5.skib.feedback.dto.ResponseTestTagDto;
import com.t1f5.skib.feedback.dto.ResponseTrainerTestStatisticsDto;
import com.t1f5.skib.feedback.dto.ScoreRangeCountDto;
import com.t1f5.skib.feedback.dto.TrainerFeedBackDto;
import com.t1f5.skib.feedback.dto.projection.AnswerMatrixProjection;
import com.t1f5.skib.feedback.repository.FeedbackDocumentQueryRepository;
import com.t1f5.skib.feedback.repository.FeedbackQuestionMongoRepository;
import com.t1f5.skib.feedback.repository.FeedbackUserAnswerRepository;
import com.t1f5.skib.feedback.repository.FeedbackUserTestRepository;
import com.t1f5.skib.question.domain.Question;
import com.t1f5.skib.question.repository.QuestionMongoRepository;
import com.t1f5.skib.test.domain.Test;
import com.t1f5.skib.test.domain.TestQuestion;
import com.t1f5.skib.test.repository.TestQuestionRepository;
import com.t1f5.skib.test.repository.TestRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class FeedbackService {

  private final FeedbackUserAnswerRepository feedbackUserAnswerRepository;
  private final FeedbackQuestionMongoRepository feedbackQuestionMongoRepository;
  private final FeedbackDocumentQueryRepository feedbackDocumentQueryRepository;
  private final FeedbackUserTestRepository feedbackUserTestRepository;
  private final QuestionMongoRepository questionMongoRepository;
  private final AnswerRepository answerRepository;
  private final TestRepository testRepository;
  private final WebClient webClient;
  private final TestQuestionRepository testQuestionRepository;

  /**
   * 사용자의 테스트에 대한 전체 정확도 비율을 가져옵니다.
   *
   * @param userId 사용자의 ID
   * @param testId 테스트의 ID
   * @return 정확도 비율, 정답 개수, 총 문항 수를 포함하는 ResponseFeedbackAllDto
   */
  public ResponseFeedbackAllDto getTotalAccuracyRate(Integer userId, Integer testId) {
    Integer userTestId = feedbackUserTestRepository.findUserTestIdByUserIdAndTestId(userId, testId);

    Object[] row = feedbackUserAnswerRepository.getTotalAccuracyRateByUserTestId(userTestId);

    Long correctCount = (row[0] != null) ? (Long) row[0] : 0L;
    Long totalCount = (row[1] != null) ? (Long) row[1] : 0L;

    double rate = 0.0;
    if (totalCount > 0) {
      rate = 100.0 * correctCount / totalCount;
    }

    return ResponseFeedbackAllDto.builder()
        .accuracyRate(rate)
        .correctCount(correctCount)
        .totalCount(totalCount)
        .build();
  }

  /**
   * 사용자 답변을 기반으로 각 문서에 대한 정확도 비율을 가져옵니다.
   *
   * @param userId 사용자의 ID
   * @param testId 테스트의 ID
   * @return 문서 정확도 비율을 포함하는 ResponseFeedbackDocDto 리스트
   */
  public List<ResponseFeedbackDocDto> getDocumentAccuracyRates(Integer userId, Integer testId) {
    Integer userTestId = feedbackUserTestRepository.findUserTestIdByUserIdAndTestId(userId, testId);

    List<Object[]> answers = feedbackUserAnswerRepository.getAnswersByUserTestId(userTestId);

    Set<String> questionIds =
        answers.stream().map(row -> String.valueOf(row[0])).collect(Collectors.toSet());

    List<Question> questions = feedbackQuestionMongoRepository.findByIdIn(questionIds);

    Map<String, String> questionIdToDocumentIdMap =
        questions.stream().collect(Collectors.toMap(Question::getId, Question::getDocumentId));

    Set<Integer> documentIdSet =
        questions.stream()
            .map(
                q -> {
                  try {
                    return Integer.parseInt(q.getDocumentId());
                  } catch (Exception e) {
                    return null;
                  }
                })
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

    Map<Integer, String> documentIdToNameMap =
        feedbackDocumentQueryRepository.findByDocumentIdIn(documentIdSet).stream()
            .collect(Collectors.toMap(Document::getDocumentId, Document::getName));

    Map<String, long[]> documentStats = new HashMap<>();

    for (Object[] row : answers) {
      String questionId = String.valueOf(row[0]);
      boolean isCorrect = (Boolean) row[1];

      String documentIdStr = questionIdToDocumentIdMap.get(questionId);
      if (documentIdStr == null) continue;

      documentStats.putIfAbsent(documentIdStr, new long[2]);
      long[] stats = documentStats.get(documentIdStr);

      if (isCorrect) stats[0]++;
      stats[1]++;
    }

    List<ResponseFeedbackDocDto> result =
        documentStats.entrySet().stream()
            .map(
                entry -> {
                  String documentIdStr = entry.getKey();
                  long correctCount = entry.getValue()[0];
                  long totalCount = entry.getValue()[1];
                  double accuracyRate =
                      (totalCount > 0) ? (100.0 * correctCount / totalCount) : 0.0;

                  Integer documentIdInt = null;
                  try {
                    documentIdInt = Integer.parseInt(documentIdStr);
                  } catch (Exception ignored) {
                  }

                  String documentName =
                      (documentIdInt != null)
                          ? documentIdToNameMap.getOrDefault(documentIdInt, "Unknown")
                          : "Unknown";

                  return ResponseFeedbackDocDto.builder()
                      .documentId(documentIdStr)
                      .documentName(documentName)
                      .accuracyRate(accuracyRate)
                      .correctCount(correctCount)
                      .totalCount(totalCount)
                      .build();
                })
            .collect(Collectors.toList());

    return result;
  }

  /**
   * 사용자 답변을 기반으로 각 태그에 대한 정확도 비율을 가져옵니다.
   *
   * @param userId 사용자의 ID
   * @param testId 테스트의 ID
   * @return 태그 정확도 비율을 포함하는 ResponseFeedbackTagDto 리스트
   */
  public List<ResponseFeedbackTagDto> getTagAccuracyRates(Integer userId, Integer testId) {
    Integer userTestId = feedbackUserTestRepository.findUserTestIdByUserIdAndTestId(userId, testId);

    List<Object[]> answers = feedbackUserAnswerRepository.getAnswersByUserTestId(userTestId);

    Set<String> questionIds =
        answers.stream().map(row -> String.valueOf(row[0])).collect(Collectors.toSet());

    List<Question> questions = feedbackQuestionMongoRepository.findByIdIn(questionIds);

    Map<String, Question> questionIdToQuestionMap =
        questions.stream().collect(Collectors.toMap(Question::getId, q -> q));

    Map<String, long[]> tagStats = new HashMap<>();

    for (Object[] row : answers) {
      String questionId = String.valueOf(row[0]);
      boolean isCorrect = (Boolean) row[1];

      Question question = questionIdToQuestionMap.get(questionId);
      if (question == null) continue;

      List<String> tags = question.getTags();
      if (tags == null || tags.isEmpty()) continue;

      for (String tag : tags) {
        tagStats.putIfAbsent(tag, new long[2]);
        long[] stats = tagStats.get(tag);

        if (isCorrect) stats[0]++;
        stats[1]++;
      }
    }

    List<ResponseFeedbackTagDto> result =
        tagStats.entrySet().stream()
            .map(
                entry -> {
                  String tagName = entry.getKey();
                  long correctCount = entry.getValue()[0];
                  long totalCount = entry.getValue()[1];
                  double accuracyRate =
                      (totalCount > 0) ? (100.0 * correctCount / totalCount) : 0.0;

                  return ResponseFeedbackTagDto.builder()
                      .tagName(tagName)
                      .accuracyRate(accuracyRate)
                      .correctCount(correctCount)
                      .totalCount(totalCount)
                      .build();
                })
            .collect(Collectors.toList());

    return result;
  }

  /**
   * 사용자의 테스트에 대한 점수 분포를 가져옵니다.
   *
   * @param userId 사용자의 ID
   * @param testId 테스트의 ID
   * @return ResponseFeedbackDistributionDto 객체에 점수 분포, 사용자 점수, 총 사용자 수를 포함
   */
  public ResponseFeedbackDistributionDto getScoreDistribution(Integer userId, Integer testId) {
    // 1️⃣ 전체 응시자 점수 가져오기
    List<Integer> allScores = feedbackUserTestRepository.findAllScoresByTestId(testId);
    Long total = (long) allScores.size();

    // 2️⃣ 내 점수 가져오기
    Integer myScore =
        feedbackUserTestRepository.findScoreByUserIdAndTestId(userId, testId).orElse(null);

    // 3️⃣ 점수 구간 설정 (예: 0~9, 10~19, ..., 90~100)
    int interval = 10;
    Map<Integer, Long> distribution = new HashMap<>();

    for (Integer score : allScores) {
      if (score == null) continue;
      int bucket = (score == 100) ? 100 : (score / interval) * interval;
      distribution.put(bucket, distribution.getOrDefault(bucket, 0L) + 1);
    }

    // 4️⃣ DTO 구성
    List<ScoreRangeCountDto> scoreList =
        distribution.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(
                entry -> {
                  int min = entry.getKey();
                  int max = (min == 100) ? 100 : min + interval - 1;
                  long count = entry.getValue();
                  double percentage = (total > 0) ? ((double) count / total * 100.0) : 0.0;
                  return ScoreRangeCountDto.builder()
                      .minScore(min)
                      .maxScore(max)
                      .userCount(count)
                      .percentage(Math.round(percentage * 10.0) / 10.0)
                      .build();
                })
            .collect(Collectors.toList());

    return ResponseFeedbackDistributionDto.builder()
        .scoreDistribution(scoreList)
        .myScore(myScore)
        .totalUserCount(total)
        .build();
  }

  /**
   * 트레이너가 특정 테스트의 문제별 피드백을 가져옵니다.
   *
   * @param testId 테스트의 ID
   * @param isDescending 정렬 여부 (내림차순)
   * @return TrainerFeedBackDto 리스트에 문제 번호, ID, 텍스트, 난이도, 유형, 정답률 등을 포함
   */
  public List<TrainerFeedBackDto> getQuestionFeedbackSortedByTestId(
      Integer testId, boolean isDescending) {
    List<TrainerFeedBackDto> feedbackList = getFeedbackList(testId);

    feedbackList.sort(Comparator.comparingDouble(TrainerFeedBackDto::getCorrectRate));
    if (isDescending) Collections.reverse(feedbackList);

    log.info("[최종 반환] Feedback 개수: {}", feedbackList.size());
    return feedbackList;
  }

  public List<TrainerFeedBackDto> getQuestionFeedbackListWithoutSorting(Integer testId) {
    return getFeedbackList(testId);
  }

  /**
   * 트레이너가 특정 테스트에 대한 피드백을 생성합니다.
   *
   * @param testId 테스트의 ID
   * @return FastAPI로부터 받은 피드백 문자열
   */
  public String generateFeedbackForTest(Integer testId) {
    List<TrainerFeedBackDto> feedbackList = getQuestionFeedbackListWithoutSorting(testId);
    if (feedbackList.isEmpty()) {
      throw new IllegalArgumentException("해당 테스트에 대한 피드백을 생성할 수 없습니다. 문제나 답변이 없습니다.");
    }

    Test test =
        testRepository
            .findById(testId)
            .orElseThrow(() -> new IllegalArgumentException("해당 테스트가 존재하지 않습니다."));

    try {
      String response =
          sendFeedbackRequest(new RequestFeedbackForLLMDto(feedbackList, test.getSummary()))
              .block();
      log.info("FastAPI 응답: {}", response);
      return response;
    } catch (Exception e) {
      log.error("FastAPI 호출 실패: {}", e.getMessage(), e);
      throw new RuntimeException("FastAPI 서버 호출 중 오류가 발생했습니다.", e);
    }
  }

  private Mono<String> sendFeedbackRequest(RequestFeedbackForLLMDto dto) {
    return webClient
        .post()
        .uri("https://skib-ai.skala25a.project.skala-ai.com/api/feedback/generate")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(dto)
        .retrieve()
        .onStatus(
            status -> !status.is2xxSuccessful(),
            clientResponse -> clientResponse.bodyToMono(String.class).map(RuntimeException::new))
        .bodyToMono(String.class);
  }

  /**
   * 트레이너가 특정 테스트의 통계 정보를 가져옵니다.
   *
   * @param testId 테스트의 ID
   * @return ResponseTrainerTestStatisticsDto 객체에 평균 점수, 합격자 수, 응시자 수, 합격 기준 점수를 포함
   */
  public ResponseTrainerTestStatisticsDto getTestBasicStatistics(Integer testId) {
    Test test =
        testRepository
            .findById(testId)
            .orElseThrow(() -> new IllegalArgumentException("해당 테스트가 존재하지 않습니다."));

    Integer passScore = test.getPassScore();
    Double averageScore = feedbackUserTestRepository.findAverageScoreByTestId(testId);
    Integer passCount = feedbackUserTestRepository.countPassUsersByTestId(testId, passScore);
    Integer totalTakers = feedbackUserTestRepository.countTotalUsersByTestId(testId);

    return ResponseTrainerTestStatisticsDto.builder()
        .averageScore(averageScore != null ? Math.round(averageScore * 10.0) / 10.0 : 0.0)
        .passCount(passCount)
        .totalTakers(totalTakers)
        .passScore(passScore)
        .build();
  }

  /**
   * 특정 테스트에 대한 학습자별 문제별 정답 현황을 가져옵니다.
   *
   * @param testId 테스트 ID
   * @return ResponseAnswerMatrixDto 객체에 문제 레이블과 학습자별 정답 여부 리스트를 포함
   */
  public ResponseAnswerMatrixDto getAnswerMatrix(Integer testId) {
    List<AnswerMatrixProjection> rows =
        feedbackUserAnswerRepository.findAnswerMatrixByTestId(testId);

    // 1. 문제 번호 정렬
    List<Integer> questionNumbers =
        rows.stream().map(AnswerMatrixProjection::getQuestionNumber).distinct().sorted().toList();

    // 2. 사용자별 문제번호 → 정답 여부 Map 구성
    Map<String, Map<Integer, Boolean>> userMap = new LinkedHashMap<>();
    for (AnswerMatrixProjection row : rows) {
      userMap
          .computeIfAbsent(row.getUserName(), k -> new HashMap<>())
          .put(row.getQuestionNumber(), row.getIsCorrect());
    }

    // 3. DTO 변환
    List<AnswerRow> userAnswers = new ArrayList<>();
    for (Map.Entry<String, Map<Integer, Boolean>> entry : userMap.entrySet()) {
      List<Boolean> correctnessList =
          questionNumbers.stream().map(qn -> entry.getValue().getOrDefault(qn, false)).toList();

      userAnswers.add(
          AnswerRow.builder().userName(entry.getKey()).correctnessList(correctnessList).build());
    }

    // 4. 최종 DTO 반환
    List<String> questionLabels = questionNumbers.stream().map(qn -> "문제" + qn).toList();

    return ResponseAnswerMatrixDto.builder()
        .questionLabels(questionLabels)
        .userAnswers(userAnswers)
        .build();
  }

  /**
   * 특정 테스트에 대한 태그별 정답률을 가져옵니다.
   *
   * @param testId 테스트 ID
   * @return ResponseTestTagDto 리스트에 태그 이름과 정답률을 포함
   */
  public List<ResponseTestTagDto> getTagAccuracyRatesByTestId(Integer testId) {
    // 1. 테스트에 포함된 문제들
    List<TestQuestion> testQuestions = testQuestionRepository.findByTest_TestId(testId);
    Set<String> questionIds =
        testQuestions.stream().map(TestQuestion::getQuestionId).collect(Collectors.toSet());

    // 2. MongoDB에서 태그 조회
    List<Question> questions = feedbackQuestionMongoRepository.findByIdIn(questionIds);

    // 3. 각 문제별 정답률 계산
    List<Answer> answers = feedbackUserAnswerRepository.findByQuestionIdIn(questionIds);
    Map<String, List<Answer>> groupedByQuestion =
        answers.stream().collect(Collectors.groupingBy(Answer::getQuestionId));

    Map<String, Double> questionAccuracyMap = new HashMap<>();
    for (Map.Entry<String, List<Answer>> entry : groupedByQuestion.entrySet()) {
      long correct = entry.getValue().stream().filter(Answer::getIsCorrect).count();
      long total = entry.getValue().size();
      questionAccuracyMap.put(entry.getKey(), total > 0 ? (correct * 100.0 / total) : 0.0);
    }

    // 4. 태그 기준 평균 정답률 계산
    Map<String, List<Double>> tagAccuracyMap = new HashMap<>();
    for (Question q : questions) {
      Double accuracy = questionAccuracyMap.getOrDefault(q.getId(), 0.0);
      for (String tag : q.getTags()) {
        tagAccuracyMap.computeIfAbsent(tag, k -> new ArrayList<>()).add(accuracy);
      }
    }

    return tagAccuracyMap.entrySet().stream()
        .map(
            entry -> {
              List<Double> scores = entry.getValue();
              double avg = scores.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
              return ResponseTestTagDto.builder()
                  .tagName(entry.getKey())
                  .accuracyRate(Math.round(avg * 10.0) / 10.0)
                  .build();
            })
        .collect(Collectors.toList());
  }

  private List<TrainerFeedBackDto> getFeedbackList(Integer testId) {
    Map<String, Integer> questionNumberMap = getQuestionNumberMap(testId);
    Map<String, Double> correctRateMap = getCorrectRateMap(testId);

    List<Question> questions = questionMongoRepository.findAll();
    log.info("[MongoDB] 문제 수: {}", questions.size());

    return questions.stream()
        .filter(q -> correctRateMap.containsKey(q.getId()))
        .map(q -> mapToFeedbackDto(q, questionNumberMap, correctRateMap))
        .collect(Collectors.toList());
  }

  private Map<String, Integer> getQuestionNumberMap(Integer testId) {
    return testQuestionRepository.findByTest_TestIdAndIsDeletedFalse(testId).stream()
        .collect(Collectors.toMap(TestQuestion::getQuestionId, TestQuestion::getQuestionNumber));
  }

  private Map<String, Double> getCorrectRateMap(Integer testId) {
    List<QuestionCorrectRateProjection> rateList =
        answerRepository.findCorrectRatesByTestId(testId);
    log.info("[정답률 쿼리 결과] {}건", rateList.size());

    return rateList.stream()
        .collect(
            Collectors.toMap(
                QuestionCorrectRateProjection::getQuestionId,
                p -> {
                  if (p.getTotalCount() == 0) return 0.0;
                  return Math.round(((double) p.getCorrectCount() / p.getTotalCount()) * 10000.0)
                      / 100.0;
                }));
  }

  private TrainerFeedBackDto mapToFeedbackDto(
      Question q, Map<String, Integer> numberMap, Map<String, Double> rateMap) {
    double correctRate = rateMap.getOrDefault(q.getId(), 0.0);
    Integer questionNumber = numberMap.getOrDefault(q.getId(), null);

    log.info(
        "[정답률 계산] questionId={}, correctRate={}, questionNumber={}",
        q.getId(),
        correctRate,
        questionNumber);

    return TrainerFeedBackDto.builder()
        .questionNumber(questionNumber)
        .questionId(q.getId())
        .documentId(q.getDocumentId())
        .questionText(q.getQuestion())
        .difficulty(q.getDifficultyLevel())
        .type(q.getType())
        .answer(q.getAnswer())
        .tags(q.getTags())
        .correctRate(correctRate)
        .build();
  }
}

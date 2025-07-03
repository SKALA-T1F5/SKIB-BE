package com.t1f5.skib.feedback.service;

import com.t1f5.skib.answer.domain.Answer;
import com.t1f5.skib.answer.repository.AnswerRepository;
import com.t1f5.skib.answer.repository.QuestionCorrectRateProjection;
import com.t1f5.skib.feedback.dto.FeedbackGenerationResponseDto;
import com.t1f5.skib.feedback.dto.RequestFeedbackForLLMDto;
import com.t1f5.skib.feedback.dto.ResponseAnswerMatrixDto;
import com.t1f5.skib.feedback.dto.ResponseFeedbackAllDto;
import com.t1f5.skib.feedback.dto.ResponseFeedbackDistributionDto;
import com.t1f5.skib.feedback.dto.ResponseFeedbackDocDto;
import com.t1f5.skib.feedback.dto.ResponseFeedbackTagDto;
import com.t1f5.skib.feedback.dto.ResponseTestTagDto;
import com.t1f5.skib.feedback.dto.ResponseTrainerTestStatisticsDto;
import com.t1f5.skib.feedback.dto.ScoreRangeCountDto;
import com.t1f5.skib.feedback.dto.TrainerFeedBackDto;
import com.t1f5.skib.feedback.dto.projection.AnswerMatrixProjection;
import com.t1f5.skib.feedback.repository.FeedbackQuestionMongoRepository;
import com.t1f5.skib.feedback.repository.FeedbackUserAnswerRepository;
import com.t1f5.skib.feedback.repository.FeedbackUserTestRepository;
import com.t1f5.skib.question.domain.Question;
import com.t1f5.skib.question.repository.QuestionMongoRepository;
import com.t1f5.skib.test.domain.Test;
import com.t1f5.skib.test.domain.TestQuestion;
import com.t1f5.skib.test.domain.UserTest;
import com.t1f5.skib.test.repository.TestQuestionRepository;
import com.t1f5.skib.test.repository.TestRepository;
import com.t1f5.skib.test.repository.UserTestRepository;
import com.t1f5.skib.user.model.User;
import com.t1f5.skib.user.repository.UserRepository;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class FeedbackService {

  private final FeedbackUserAnswerRepository feedbackUserAnswerRepository;
  private final FeedbackQuestionMongoRepository feedbackQuestionMongoRepository;
  private final FeedbackUserTestRepository feedbackUserTestRepository;
  private final QuestionMongoRepository questionMongoRepository;
  private final AnswerRepository answerRepository;
  private final UserRepository userRepository;
  private final TestRepository testRepository;
  private final UserTestRepository userTestRepository;
  private final WebClient webClient;
  private final TestQuestionRepository testQuestionRepository;

  @Value("${fastapi.base-url}")
  private String fastApiBaseUrl;

  /**
   * 사용자의 테스트에 대한 전체 정확도 비율을 가져옵니다.
   *
   * @param userId 사용자의 ID
   * @param testId 테스트의 ID
   * @return 정확도 비율, 정답 개수, 총 문항 수를 포함하는 ResponseFeedbackAllDto
   */
  @Transactional(readOnly = true)
  public ResponseFeedbackAllDto getFeedbackSummary(Integer userId, Integer testId) {
    // 1. 유저 테스트 가져오기
    UserTest userTest = feedbackUserTestRepository.findByUserIdAndTestId(userId, testId);
    Integer userTestId = userTest.getUserTestId();
    System.out.println("🎯 userTestId = " + userTestId);

    // 2. 해당 유저 테스트에 연결된 Answer 리스트 조회
    List<Answer> answers = answerRepository.findAllByUserTest_UserTestId(userTestId);

    // 3. 정답/오답 개수 계산
    long correctCount = answers.stream().filter(a -> Boolean.TRUE.equals(a.getIsCorrect())).count();

    long incorrectCount =
        answers.stream().filter(a -> Boolean.FALSE.equals(a.getIsCorrect())).count();

    Integer passScore = testRepository.findPassScoreByTestId(testId).orElse(0);

    // 5. 결과 DTO 반환
    return ResponseFeedbackAllDto.builder()
        .totalScore(userTest.getScore())
        .passScore(passScore)
        .correctCount(correctCount)
        .incorrectCount(incorrectCount)
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
    // 1. 유저 테스트 조회
    Integer userTestId = feedbackUserTestRepository.findUserTestIdByUserIdAndTestId(userId, testId);

    if (userTestId == null) {
      return Collections.emptyList();
    }

    // 2. Answer에서 documentId, documentName, isCorrect 가져오기
    List<Answer> answers = feedbackUserAnswerRepository.findByUserTest_UserTestId(userTestId);

    if (answers.isEmpty()) {
      return Collections.emptyList();
    }

    // 3. 문서별 정답률 집계
    Map<Integer, long[]> documentStats = new HashMap<>();
    Map<Integer, String> documentNames = new HashMap<>();

    for (Answer answer : answers) {
      Integer documentId = answer.getDocumentId();
      if (documentId == null) continue;

      documentStats.putIfAbsent(documentId, new long[2]);
      long[] stats = documentStats.get(documentId);

      if (Boolean.TRUE.equals(answer.getIsCorrect())) {
        stats[0]++; // correct count
      }
      stats[1]++; // total count

      documentNames.putIfAbsent(documentId, answer.getDocumentName());
    }

    // 4. 결과 변환
    List<ResponseFeedbackDocDto> result =
        documentStats.entrySet().stream()
            .map(
                entry -> {
                  Integer docId = entry.getKey();
                  long correct = entry.getValue()[0];
                  long total = entry.getValue()[1];
                  double accuracy = total > 0 ? (100.0 * correct / total) : 0.0;

                  return ResponseFeedbackDocDto.builder()
                      .documentId(docId.toString())
                      .documentName(documentNames.getOrDefault(docId, "Unknown"))
                      .accuracyRate(accuracy)
                      .correctCount(correct)
                      .totalCount(total)
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
   * 특정 테스트에 대한 문제별 피드백을 가져옵니다.
   *
   * @param testId 테스트의 ID
   * @return TrainerFeedBackDto 리스트에 문제 번호, ID, 텍스트, 난이도, 유형, 정답률 등을 포함
   */
  public FeedbackGenerationResponseDto generateFeedbackForTest(Integer testId) {
    List<TrainerFeedBackDto> feedbackList = getQuestionFeedbackListWithoutSorting(testId);
    if (feedbackList.isEmpty()) {
      throw new IllegalArgumentException("해당 테스트에 대한 피드백을 생성할 수 없습니다. 문제나 답변이 없습니다.");
    }

    Test test =
        testRepository
            .findById(testId)
            .orElseThrow(() -> new IllegalArgumentException("해당 테스트가 존재하지 않습니다."));

    try {
      FeedbackGenerationResponseDto response =
          sendFeedbackRequest(new RequestFeedbackForLLMDto(feedbackList, test.getSummary()))
              .block(); // ⬅️ 이 줄에서 바로 DTO로 받도록 수정

      log.info("FastAPI 응답 수신 성공: {}", response);
      return response;

    } catch (Exception e) {
      log.error("FastAPI 호출 실패: {}", e.getMessage(), e);
      throw new RuntimeException("FastAPI 서버 호출 중 오류가 발생했습니다.", e);
    }
  }

  private Mono<FeedbackGenerationResponseDto> sendFeedbackRequest(RequestFeedbackForLLMDto dto) {
    return webClient
        .post()
        .uri(fastApiBaseUrl + "api/feedback/generate")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(dto)
        .retrieve()
        .onStatus(
            status -> !status.is2xxSuccessful(),
            clientResponse -> clientResponse.bodyToMono(String.class).map(RuntimeException::new))
        .bodyToMono(FeedbackGenerationResponseDto.class);
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

  public ResponseAnswerMatrixDto getAnswerMatrix(Integer testId) {
    // 0. 먼저 유저 ID 전체 조회 (이 테스트에 참여한 유저들)
    List<UserTest> userTests = userTestRepository.findByTest_TestId(testId);

    // 1. userId → userTestId 매핑
    Map<Integer, Integer> userToTestMap = new HashMap<>();
    for (UserTest ut : userTests) {
      userToTestMap.put(ut.getUser().getUserId(), ut.getUserTestId());
    }

    // 2. 모든 user_test_id로 정답 정보 가져오기
    List<AnswerMatrixProjection> rows =
        feedbackUserAnswerRepository.findAnswerMatrixByUserTestIds(
            new ArrayList<>(userToTestMap.values()));

    // 3. 문제 번호 추출
    List<Integer> questionNumbers =
        rows.stream()
            .map(AnswerMatrixProjection::getQuestionNumber)
            .filter(Objects::nonNull)
            .distinct()
            .sorted()
            .toList();

    // 4. user_test_id 기준으로 정답 맵 구성
    Map<Integer, Map<Integer, Boolean>> userTestMap = new LinkedHashMap<>();
    Map<Integer, Integer> testToUserMap = new HashMap<>();

    for (AnswerMatrixProjection row : rows) {
      Integer userTestId = row.getUserTestId();
      Integer userId = row.getUserId();

      userTestMap
          .computeIfAbsent(userTestId, k -> new HashMap<>())
          .put(row.getQuestionNumber(), row.getIsCorrect());

      testToUserMap.putIfAbsent(userTestId, userId);
    }

    // 5. DTO로 변환
    // DTO 변환
    List<ResponseAnswerMatrixDto.AnswerRow> userAnswers =
        userTestMap.entrySet().stream()
            .map(
                entry -> {
                  Integer userTestId = entry.getKey();
                  Map<Integer, Boolean> answerMap = entry.getValue();

                  // userId 매핑
                  Integer userId = testToUserMap.get(userTestId);

                  // ✅ 정답 여부
                  List<Boolean> correctnessList =
                      questionNumbers.stream()
                          .map(qNum -> answerMap.getOrDefault(qNum, false))
                          .toList();

                  // ✅ 이름 조회 (없으면 "이름없음")
                  String userName =
                      userRepository.findById(userId).map(User::getName).orElse("이름없음");

                  return new ResponseAnswerMatrixDto.AnswerRow(userId, userName, correctnessList);
                })
            .toList();

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

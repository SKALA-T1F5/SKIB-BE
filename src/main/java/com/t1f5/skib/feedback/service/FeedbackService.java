package com.t1f5.skib.feedback.service;

import com.t1f5.skib.document.domain.Document;
import com.t1f5.skib.feedback.dto.ResponseFeedbackAllDto;
import com.t1f5.skib.feedback.dto.ResponseFeedbackDocDto;
import com.t1f5.skib.feedback.dto.ResponseFeedbackTagDto;
import com.t1f5.skib.feedback.repository.FeedbackDocumentQueryRepository;
import com.t1f5.skib.feedback.repository.FeedbackQuestionMongoRepository;
import com.t1f5.skib.feedback.repository.FeedbackUserAnswerRepository;
import com.t1f5.skib.feedback.repository.FeedbackUserTestRepository;
import com.t1f5.skib.question.domain.Question;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackUserAnswerRepository userAnswerRepository;
    private final FeedbackQuestionMongoRepository questionMongoRepository;
    private final FeedbackDocumentQueryRepository feedbackDocumentQueryRepository;
    private final FeedbackUserTestRepository userTestRepository;

    /**
     * 사용자의 테스트에 대한 전체 정확도 비율을 가져옵니다.
     *
     * @param userId 사용자의 ID
     * @param testId 테스트의 ID
     * @return 정확도 비율, 정답 개수, 총 문항 수를 포함하는 ResponseFeedbackAllDto
     */
    public ResponseFeedbackAllDto getTotalAccuracyRate(Integer userId, Integer testId) {
        Integer userTestId = userTestRepository.findUserTestIdByUserIdAndTestId(userId, testId);

        Object[] row = userAnswerRepository.getTotalAccuracyRateByUserTestId(userTestId);

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
        Integer userTestId = userTestRepository.findUserTestIdByUserIdAndTestId(userId, testId);

        List<Object[]> answers = userAnswerRepository.getAnswersByUserTestId(userTestId);

        Set<String> questionIds =
                answers.stream().map(row -> String.valueOf(row[0])).collect(Collectors.toSet());

        List<Question> questions = questionMongoRepository.findByIdIn(questionIds);

        Map<String, String> questionIdToDocumentIdMap =
                questions.stream().collect(Collectors.toMap(Question::getId, Question::getDocumentId));

        Set<Integer> documentIdSet =
                questions.stream()
                        .map(q -> {
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
                        .map(entry -> {
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
        Integer userTestId = userTestRepository.findUserTestIdByUserIdAndTestId(userId, testId);

        List<Object[]> answers = userAnswerRepository.getAnswersByUserTestId(userTestId);

        Set<String> questionIds =
                answers.stream().map(row -> String.valueOf(row[0])).collect(Collectors.toSet());

        List<Question> questions = questionMongoRepository.findByIdIn(questionIds);

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
                        .map(entry -> {
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
}

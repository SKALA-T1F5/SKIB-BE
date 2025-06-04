package com.t1f5.skib.answer.dto;

import com.t1f5.skib.global.enums.QuestionType;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ScoredAnswerResultDto {
  private String questionId;
  private QuestionType questionType;
  private String questionText;
  private List<String> choices; // 객관식 선지
  private String solution; // 정답 해설
  private List<String> gradingCriteria; // 주관식 채점 기준
  private String userResponse;
  private String correctAnswer;
  private boolean isCorrect;
}

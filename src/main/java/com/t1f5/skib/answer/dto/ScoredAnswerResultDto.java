package com.t1f5.skib.answer.dto;

import com.t1f5.skib.global.enums.QuestionType;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ScoredAnswerResultDto {
  private String questionId; // 문제 ID
  private QuestionType type; // 문제 유형 (객관식, 서술형 등)
  private String question; // 문제 본문
  private List<String> options; // 객관식 선지
  private String explanation; // 정답 해설
  private String response; // 사용자의 응답 (객관식 선택지 또는 서술형 답변)
  private String answer; // 정답
  private boolean isCorrect; // 정답 여부
}

package com.t1f5.skib.feedback.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResponseFeedbackAllDto {
  private Integer totalScore;      // 총 점수
  private Integer passScore;       // 합격 점수
  private Long correctCount;       // 정답 수
  private Long incorrectCount;     // 오답 수 (= 총 문제 수 - 정답 수)
}

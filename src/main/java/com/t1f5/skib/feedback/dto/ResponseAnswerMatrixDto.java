package com.t1f5.skib.feedback.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 테스트별 학습자별 문제별 정답 현황 DTO */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResponseAnswerMatrixDto {

  // 문제번호 (순서에 따라 정렬된 문제 ID 또는 이름)
  private List<String> questionLabels;

  // 학습자별 정답 여부 리스트
  private List<AnswerRow> userAnswers;

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class AnswerRow {
    private Integer userId; // 학습자 ID
    private List<Boolean> correctnessList; // 문제별 정답 여부: true(정답), false(오답)
  }
}
package com.t1f5.skib.answer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseSubjectiveAnswerDto {
  private String userAnswerId;
  private String questionId;
  private int score;
}

package com.t1f5.skib.answer.dto;

import com.t1f5.skib.global.enums.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnswerRequest {
  private String id;
  private String response;
  private QuestionType questionType;
}

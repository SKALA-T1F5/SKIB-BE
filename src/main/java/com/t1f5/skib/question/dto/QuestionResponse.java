package com.t1f5.skib.question.dto;

import java.util.List;
import lombok.Data;

@Data
public class QuestionResponse {
  private List<QuestionDto> questions;
}

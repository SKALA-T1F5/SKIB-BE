package com.t1f5.skib.answer.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestCreateAnswerDto {
  private List<AnswerRequest> answers;
}

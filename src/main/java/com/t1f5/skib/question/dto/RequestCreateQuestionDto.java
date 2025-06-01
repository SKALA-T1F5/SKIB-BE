package com.t1f5.skib.question.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestCreateQuestionDto {
  private List<DocumentQuestionRequest> documents;
}

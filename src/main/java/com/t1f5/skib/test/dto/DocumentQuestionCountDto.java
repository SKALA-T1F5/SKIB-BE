package com.t1f5.skib.test.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DocumentQuestionCountDto {
  private Integer documentId;
  private String documentName;
  private Integer questionCount;
}

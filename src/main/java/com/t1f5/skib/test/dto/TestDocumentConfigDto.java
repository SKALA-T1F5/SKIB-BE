package com.t1f5.skib.test.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TestDocumentConfigDto {
  private Integer documentId;
  private int configuredObjectiveCount;
  private int configuredSubjectiveCount;
}

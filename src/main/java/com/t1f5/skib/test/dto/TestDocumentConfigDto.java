package com.t1f5.skib.test.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TestDocumentConfigDto {
  private Integer documentId;
  private String documentName;
  private List<String> keywords;
  private int configuredObjectiveCount;
  private int configuredSubjectiveCount;
}

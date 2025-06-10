package com.t1f5.skib.test.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestCreateTestByLLMDto {
  private Integer projectId;
  private String userInput;
  private List<Integer> documentIds;
}

package com.t1f5.skib.test.dto;

import com.t1f5.skib.global.enums.DifficultyLevel;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ResponseCreateTestByLLMDto {
  private String name;
  private String summary;
  private DifficultyLevel difficultyLevel;
  private Integer limitedTime;
  private Integer passScore;
  private Boolean isRetake;
  private List<TestDocumentConfigDto> documentConfigs;
}

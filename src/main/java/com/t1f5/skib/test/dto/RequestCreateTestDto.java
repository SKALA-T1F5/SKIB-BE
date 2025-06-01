package com.t1f5.skib.test.dto;

import com.t1f5.skib.global.enums.DifficultyLevel;
import jakarta.validation.Valid;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestCreateTestDto {
  private String name;
  private DifficultyLevel difficultyLevel;
  private Integer limitedTime;
  private Integer passScore;
  private Boolean isRetaken;

  @Valid private List<TestDocumentConfigDto> documentConfigs;
}

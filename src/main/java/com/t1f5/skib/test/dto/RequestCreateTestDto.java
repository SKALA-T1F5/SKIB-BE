package com.t1f5.skib.test.dto;


import com.t1f5.skib.global.enums.DifficultyLevel;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestCreateTestDto {
  @Schema(hidden = true)
  private Integer testId;
  private String name;
  private String summary;
  private DifficultyLevel difficultyLevel;
  private Integer limitedTime;
  private Integer passScore;
  private Boolean isRetake;

  @Valid private List<TestDocumentConfigDto> documentConfigs;
}

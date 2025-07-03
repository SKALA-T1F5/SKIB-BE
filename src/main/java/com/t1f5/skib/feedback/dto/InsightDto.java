package com.t1f5.skib.feedback.dto;

import com.google.auto.value.AutoValue.Builder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
@Schema(description = "강점 및 약점 인사이트 DTO")
public class InsightDto {

  @Schema(description = "인사이트 유형 (강점 또는 약점)")
  private String type; // "strength" or "weakness"

  @Schema(description = "인사이트 설명")
  private String text;

  // Getters, Setters, Constructors
}

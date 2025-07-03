package com.t1f5.skib.feedback.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue.Builder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
@Schema(description = "문서별 성과 분석 DTO")
public class DocumentPerformanceDto {

  @Schema(description = "문서 이름")
  @JsonProperty("documentName")
  private String documentName;

  @Schema(description = "평균 정답률")
  @JsonProperty("averageCorrectRate")
  private double averageCorrectRate;

  @Schema(description = "해당 문서에 대한 평가")
  private String comment;

  // Getters, Setters, Constructors
}

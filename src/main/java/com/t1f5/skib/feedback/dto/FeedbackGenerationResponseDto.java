package com.t1f5.skib.feedback.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue.Builder;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
@Schema(description = "피드백 생성 응답 DTO")
public class FeedbackGenerationResponseDto {

  @Schema(description = "시험 목표")
  @JsonProperty("examGoal")
  private String examGoal;

  @Schema(description = "문서별 성과 분석 리스트")
  @JsonProperty("performanceByDocument")
  private List<DocumentPerformanceDto> performanceByDocument;

  @Schema(description = "강점 및 약점 인사이트 리스트")
  private List<InsightDto> insights;

  @Schema(description = "개선점 설명")
  @JsonProperty("improvementPoints")
  private String improvementPoints;

  @Schema(description = "추천 학습 주제 리스트")
  @JsonProperty("suggestedTopics")
  private List<String> suggestedTopics;

  @Schema(description = "종합 평가")
  @JsonProperty("overallEvaluation")
  private String overallEvaluation;

  @Schema(description = "프로젝트 투입 준비도")
  @JsonProperty("projectReadiness")
  private String projectReadiness;

  // Getters, Setters, Constructors
}

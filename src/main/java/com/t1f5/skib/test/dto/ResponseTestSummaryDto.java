package com.t1f5.skib.test.dto;

import com.t1f5.skib.global.enums.DifficultyLevel;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseTestSummaryDto {
  private Integer testId;
  private String name;
  private DifficultyLevel difficultyLevel;
  private Integer score;
  private Integer limitedTime;
  private LocalDateTime createdAt;
  private Boolean isPassed;
  private Boolean retake;
  private Boolean isRetake;
  private Integer passScore;
}

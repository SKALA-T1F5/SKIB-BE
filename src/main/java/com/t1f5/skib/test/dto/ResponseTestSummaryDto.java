package com.t1f5.skib.test.dto;

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
  private Integer limitedTime;
  private LocalDateTime createdAt;
}

package com.t1f5.skib.feedback.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseTrainerTestStatisticsDto {
    private Double averageScore;
    private Integer passCount;
    private Integer totalTakers;
    private Integer passScore;
}

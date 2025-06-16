package com.t1f5.skib.feedback.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ScoreRangeCountDto {
    private Integer minScore;
    private Integer maxScore;
    private Long userCount;
    private Double percentage;
}

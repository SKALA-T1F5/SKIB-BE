package com.t1f5.skib.feedback.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class ResponseFeedbackDistributionDto {
    private List<ScoreRangeCountDto> scoreDistribution;
    private Integer myScore;
    private Long totalUserCount;
}

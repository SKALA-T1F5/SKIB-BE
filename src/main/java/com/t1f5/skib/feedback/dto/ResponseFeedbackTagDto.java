package com.t1f5.skib.feedback.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResponseFeedbackTagDto {
    private String tagName;      // 태그명
    private Double accuracyRate; // 정답률 %
    private Long correctCount;   // 맞춘 문제 수
    private Long totalCount;     // 총 문제 수
}

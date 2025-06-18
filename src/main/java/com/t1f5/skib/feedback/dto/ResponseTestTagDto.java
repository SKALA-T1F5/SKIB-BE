package com.t1f5.skib.feedback.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResponseTestTagDto {
    private String tagName;
    private Double accuracyRate;  // 예: 72.5%
}

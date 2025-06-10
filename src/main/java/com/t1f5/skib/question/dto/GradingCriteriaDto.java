package com.t1f5.skib.question.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradingCriteriaDto {
    private Integer score;
    private String criteria;
    private String example;
    private String note;
    
}

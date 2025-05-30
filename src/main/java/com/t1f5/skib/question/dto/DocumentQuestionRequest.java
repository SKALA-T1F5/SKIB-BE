package com.t1f5.skib.question.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentQuestionRequest {
    private Integer documentId;
    private Integer configuredObjectiveCount;
    private Integer configuredSubjectiveCount;
}

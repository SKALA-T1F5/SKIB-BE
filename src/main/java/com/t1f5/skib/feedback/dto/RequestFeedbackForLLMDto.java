package com.t1f5.skib.feedback.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestFeedbackForLLMDto {
  private List<TrainerFeedBackDto> feedbacks;
  private String testSummary;
}

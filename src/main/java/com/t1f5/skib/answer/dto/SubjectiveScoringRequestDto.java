package com.t1f5.skib.answer.dto;

import com.t1f5.skib.question.dto.GradingCriteriaDto;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SubjectiveScoringRequestDto {
  private String questionId;
  private List<GradingCriteriaDto> grading_criteria;
  private String response;
}

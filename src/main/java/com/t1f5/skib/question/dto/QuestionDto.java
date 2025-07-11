package com.t1f5.skib.question.dto;

import com.t1f5.skib.global.enums.DifficultyLevel;
import com.t1f5.skib.global.enums.GenerationType;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionDto {
  private String id;
  private String type;
  private DifficultyLevel difficulty_level;
  private String question;
  private List<String> options;
  private String answer;
  private String explanation;
  private List<GradingCriteriaDto> grading_criteria;
  private Integer documentId;
  private String documentName;
  private List<String> keywords;
  private List<String> tags;
  private GenerationType generationType;
}
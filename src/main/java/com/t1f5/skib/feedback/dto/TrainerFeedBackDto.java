package com.t1f5.skib.feedback.dto;

import com.t1f5.skib.global.enums.DifficultyLevel;
import com.t1f5.skib.global.enums.QuestionType;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TrainerFeedBackDto {
  private String questionId;
  private String documentId;
  private String questionText;
  private DifficultyLevel difficulty;
  private QuestionType type;
  private String answer;
  private List<String> tags;
  private double correctRate;
}

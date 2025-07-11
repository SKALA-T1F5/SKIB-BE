package com.t1f5.skib.question.dto;

import com.t1f5.skib.global.enums.DifficultyLevel;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestCreateQuestionDto {
  private String name;
  private DifficultyLevel difficultyLevel;
  private Integer limitedTime;
  private Integer passScore;
  private Boolean isRetake;
  private String summary;

  private Integer documentId;
  private List<String> keywords;
  private int configuredObjectiveCount;
  private int configuredSubjectiveCount;
}

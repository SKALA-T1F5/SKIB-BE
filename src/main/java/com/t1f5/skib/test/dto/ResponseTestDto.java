package com.t1f5.skib.test.dto;

import com.t1f5.skib.global.enums.DifficultyLevel;
import com.t1f5.skib.question.dto.QuestionDto;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseTestDto {
  private Integer testId;
  private String name;
  private Integer limitedTime;
  private LocalDateTime createdAt;
  private Integer passScore;
  private DifficultyLevel difficultyLevel;

  // 추가 필드 : 테스트의 문제들
  private List<QuestionDto> questions;
}

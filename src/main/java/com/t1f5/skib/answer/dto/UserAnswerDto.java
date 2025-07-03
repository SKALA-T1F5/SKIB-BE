package com.t1f5.skib.answer.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class UserAnswerDto {
  private Integer userId;
  private List<Boolean> correctnessList;
}

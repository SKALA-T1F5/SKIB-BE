package com.t1f5.skib.test.dto;

import java.util.List;
import lombok.Data;

@Data
public class RequestFinalizeTestDto {
  private List<String> selectedQuestionIds; // 확정된 문제 ID들
  private List<String> toDeleteQuestionIds; // 전체 생성된 문제 ID들
}

package com.t1f5.skib.test.dto;

import java.time.LocalDateTime;
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
  private LocalDateTime createdAt;

  // 추가 필드 : 테스트의 문제들
}

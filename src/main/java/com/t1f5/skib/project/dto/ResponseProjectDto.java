package com.t1f5.skib.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseProjectDto {
  private Integer projectId;
  private String projectName;
  private String projectDescription;
  private String createdAt;
  // 테스트 리스트 추가
}

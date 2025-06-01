package com.t1f5.skib.project.dto;

import com.t1f5.skib.global.dtos.DtoConverter;
import com.t1f5.skib.project.domain.Project;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ProjectDtoConverter implements DtoConverter<Project, ResponseProjectDto> {

  // 단일 프로젝트를 조회할 때 사용하는 DTO 변환기
  @Override
  public ResponseProjectDto convert(Project project) {
    if (project == null) {
      return null;
    }

    return ResponseProjectDto.builder()
        .projectId(project.getProjectId())
        .projectName(project.getProjectName())
        .projectDescription(project.getProjectDescription())
        .createdAt(project.getCreatedDate().toString())
        .build();
  }
}

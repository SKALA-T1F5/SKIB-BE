package com.t1f5.skib.project.dto;

import com.t1f5.skib.user.dto.responsedto.ResponseUserDto;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseProjectUserDto {
  private Integer projectId;
  private String projectName;
  private String projectDescription;
  private String createdAt;

  private List<ResponseUserDto> trainer;
  private List<ResponseUserDto> trainee;
}

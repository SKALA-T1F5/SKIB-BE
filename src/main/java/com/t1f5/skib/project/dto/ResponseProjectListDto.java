package com.t1f5.skib.project.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResponseProjectListDto {
  private int count;
  private List<ResponseProjectDto> projects;
}

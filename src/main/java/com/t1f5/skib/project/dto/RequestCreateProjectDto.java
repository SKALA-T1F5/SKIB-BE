package com.t1f5.skib.project.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestCreateProjectDto {
  private String projectName;
  private String projectDescription;
  private List<String> trainerEmails;
}

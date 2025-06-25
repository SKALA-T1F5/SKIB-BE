package com.t1f5.skib.test.dto;

import java.util.List;
import lombok.Data;

@Data
public class RequestSaveRandomTestDto {
  private String name;
  private Integer limitedTime;
  private Integer passScore;
  private Integer projectId;
  private List<String> questionIds;
}

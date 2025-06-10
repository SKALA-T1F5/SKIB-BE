package com.t1f5.skib.test.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResponseTestListDto {
  private int count;
  private List<ResponseTestDto> tests;
}

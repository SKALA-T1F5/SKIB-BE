package com.t1f5.skib.test.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResponseTestSummaryListDto {
  private int count;
  private List<ResponseTestSummaryDto> tests;
}
package com.t1f5.skib.test.dto;

import com.t1f5.skib.global.enums.TestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TestProgressNotification {
  private Integer testId;
  private TestStatus status;
}

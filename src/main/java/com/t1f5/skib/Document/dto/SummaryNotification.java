package com.t1f5.skib.document.dto;

import com.t1f5.skib.global.enums.DocumentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SummaryNotification {
  private Integer documentId;
  private DocumentStatus status;
}

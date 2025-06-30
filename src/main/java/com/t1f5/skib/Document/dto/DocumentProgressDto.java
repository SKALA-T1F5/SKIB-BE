package com.t1f5.skib.document.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentProgressDto {
  private Integer documentId;
  private String status;
}

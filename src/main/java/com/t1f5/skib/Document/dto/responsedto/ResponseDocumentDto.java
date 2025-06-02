package com.t1f5.skib.document.dto.responsedto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseDocumentDto {
  private Integer documentId;
  private String name;
  private Long fileSize;
  private String extension;
  private Boolean isUploaded;
  private String createdAt;
}

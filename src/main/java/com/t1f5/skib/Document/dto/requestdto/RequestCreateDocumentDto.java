package com.t1f5.skib.document.dto.requestdto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestCreateDocumentDto {
  private String name;
  private String url;
  private Long fileSize;
  private String extension;
  private Boolean isUploaded;
}

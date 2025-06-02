package com.t1f5.skib.document.dto.responsedto;

import com.t1f5.skib.document.domain.Document;
import com.t1f5.skib.global.dtos.DtoConverter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class DocumentDtoConverter implements DtoConverter<Document, ResponseDocumentDto> {

  @Override
  public ResponseDocumentDto convert(Document document) {
    if (document == null) {
      return null;
    }

    return ResponseDocumentDto.builder()
        .documentId(document.getDocumentId())
        .name(document.getName())
        .fileSize(document.getFileSize())
        .extension(document.getExtension())
        .isUploaded(document.getIsUploaded())
        .createdAt(document.getCreatedDate().toString())
        .build();
  }
}

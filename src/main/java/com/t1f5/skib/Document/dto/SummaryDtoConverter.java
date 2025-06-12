package com.t1f5.skib.document.dto;

import com.t1f5.skib.document.domain.Summary;
import org.springframework.stereotype.Component;

@Component
public class SummaryDtoConverter {
  public Summary convert(SummaryDto dto, Integer documentId) {
    return Summary.builder()
        .summary(dto.getSummary())
        .keyword(dto.getKeywords())
        .documentId(documentId)
        .build();
  }
}

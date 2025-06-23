package com.t1f5.skib.document.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SummaryDto {
  private String summary;
  private List<String> keywords;
  
  @JsonProperty("document_id")  
  private Integer documentId;
}

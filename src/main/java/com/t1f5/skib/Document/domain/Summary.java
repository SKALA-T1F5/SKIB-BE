package com.t1f5.skib.document.domain;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "summary")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Summary {
  @Id private String summaryId; // MongoDB에서는 일반적으로 String ID를 사용
  private Integer documentId; // 연관된 문서 ID

  private String name; // 문서 이름
  private String summary; // 문서 요약
  private List<String> keywords; // 문서 핵심 키워드 목록
}

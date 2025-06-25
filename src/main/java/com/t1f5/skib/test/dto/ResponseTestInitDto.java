package com.t1f5.skib.test.dto;

import com.t1f5.skib.question.domain.Question;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@AllArgsConstructor
@Data
public class ResponseTestInitDto {
  Integer testId; // 생성된 테스트 ID
  List<Question> questions; // 생성된 문제들
}

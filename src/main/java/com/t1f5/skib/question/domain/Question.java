package com.t1f5.skib.question.domain;

import com.t1f5.skib.global.enums.DifficultyLevel;
import com.t1f5.skib.global.enums.GenerationType;
import com.t1f5.skib.global.enums.QuestionType;
import com.t1f5.skib.question.dto.GradingCriteriaDto;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "QUESTION")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Question {
  @Id private String id;

  private QuestionType type; // 객관식(OBJECTIVE), 서술형(SUBJECTIVE)
  private DifficultyLevel difficultyLevel; // 난이도 (예: EASY, MEDIUM, HARD)
  private String question; // 문제 본문
  private List<String> options; // 객관식 선택지 (nullable)
  private String answer; // 정답
  private List<GradingCriteriaDto> gradingCriteria; // 채점 기준 (nullable)
  private String explanation; // 해설
  private String documentId; // 문서 ID
  private String documentName; // 문서 이름
  private List<String> keywords; // 키워드 목록 (예: 문서 제목, 주요 내용 등)
  private List<String> tags; // 태그 목록 (예: 문해력, 논리력 등)
  private GenerationType generationType; // 문제 상태 (예: BASIC, EXTRA)
}

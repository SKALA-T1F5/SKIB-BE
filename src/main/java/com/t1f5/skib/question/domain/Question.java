package com.t1f5.skib.question.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.t1f5.skib.global.enums.DifficultyLevel;

import java.util.List;

@Document(collection = "QUESTION")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Question {
    @Id
    private String id;

    private String type; // 객관식(MULTIPLE_CHOICE), 서술형(SUBJECTIVE)
    private DifficultyLevel difficultyLevel; // 난이도 (예: EASY, MEDIUM, HARD)
    private String question; // 문제 본문
    private List<String> options; // 객관식 선택지 (nullable)
    private String answer; // 정답
    private String explanation; // 해설
    private String documentId; // 문서 ID
    private List<String> tags; // 태그 목록 (예: 문해력, 논리력 등)
}

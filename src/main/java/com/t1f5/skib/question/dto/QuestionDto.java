package com.t1f5.skib.question.dto;

import java.util.List;

import com.t1f5.skib.global.enums.DifficultyLevel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionDto {
    private String type;
    private DifficultyLevel difficulty_level;
    private String question;
    private List<String> options;
    private String answer;
    private String explanation;
    private String document_id;
    private List<String> tags;
}

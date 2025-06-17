package com.t1f5.skib.question.dto;

import com.t1f5.skib.global.enums.QuestionType;
import com.t1f5.skib.question.domain.Question;
import org.springframework.stereotype.Component;

@Component
public class QuestionDtoConverter {
  public Question convert(QuestionDto dto, Integer projectId) {
    return Question.builder()
        .type(QuestionType.valueOf(dto.getType()))
        .difficultyLevel(dto.getDifficulty_level())
        .question(dto.getQuestion())
        .options(dto.getOptions())
        .answer(dto.getAnswer())
        .explanation(dto.getExplanation())
        .documentId(dto.getDocument_id())
        .tags(dto.getTags())
        .build();
  }
}

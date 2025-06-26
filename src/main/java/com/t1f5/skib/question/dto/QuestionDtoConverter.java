package com.t1f5.skib.question.dto;

import com.t1f5.skib.global.enums.GenerationType;
import com.t1f5.skib.global.enums.QuestionType;
import com.t1f5.skib.question.domain.Question;
import org.springframework.stereotype.Component;

@Component
public class QuestionDtoConverter {
  public Question convert(QuestionDto dto) {
    return Question.builder()
        .type(QuestionType.valueOf(dto.getType()))
        .difficultyLevel(dto.getDifficulty_level())
        .question(dto.getQuestion())
        .options(dto.getOptions())
        .answer(dto.getAnswer())
        .explanation(dto.getExplanation())
        .gradingCriteria(dto.getGrading_criteria())
        .documentId(String.valueOf(dto.getDocumentId()))
        .documentName(dto.getDocument_name())
        .keywords(dto.getKeywords())
        .tags(dto.getTags())
        .generationType(dto.getGenerationType())
        .build();
  }
}

package com.t1f5.skib.question.dto;

import com.t1f5.skib.question.domain.Question;
import org.springframework.stereotype.Component;

@Component
public class QuestionToDtoConverter {
  public QuestionDto convert(Question entity) {
    return QuestionDto.builder()
        .id(
            entity.getId() != null
                ? entity.getId().toString()
                : null) // Convert Integer ID to String
        .type(entity.getType().name())
        .difficulty_level(entity.getDifficultyLevel())
        .question(entity.getQuestion())
        .options(entity.getOptions())
        .answer(entity.getAnswer())
        .explanation(entity.getExplanation())
        .grading_criteria(entity.getGradingCriteria())
        .documentId(
            entity.getDocumentId() != null ? Integer.parseInt(entity.getDocumentId()) : null)
        .documentName(entity.getDocumentName())
        .keywords(entity.getKeywords())
        .tags(entity.getTags())
        .generationType(entity.getGenerationType())
        .build();
  }
}

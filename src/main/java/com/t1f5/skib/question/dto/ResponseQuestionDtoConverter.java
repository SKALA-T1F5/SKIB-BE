package com.t1f5.skib.question.dto;

import com.t1f5.skib.global.dtos.DtoConverter;
import com.t1f5.skib.question.domain.Question;
import org.springframework.stereotype.Component;

@Component
public class ResponseQuestionDtoConverter implements DtoConverter<Question, QuestionDto> {

  @Override
  public QuestionDto convert(Question question) {
    return QuestionDto.builder()
        .id(
            question.getId() != null
                ? question.getId().toString()
                : null) // Convert Integer ID to String
        .type(question.getType().name())
        .difficulty_level(question.getDifficultyLevel())
        .question(question.getQuestion())
        .options(question.getOptions())
        .answer(question.getAnswer())
        .grading_criteria(question.getGradingCriteria())
        .explanation(question.getExplanation())
        .documentId(
            question.getDocumentId() != null ? Integer.valueOf(question.getDocumentId()) : null)
        .keywords(question.getKeywords())
        .tags(question.getTags())
        .generationType(question.getGenerationType())
        .build();
  }
}

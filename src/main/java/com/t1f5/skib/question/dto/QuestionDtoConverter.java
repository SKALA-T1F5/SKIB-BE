package com.t1f5.skib.question.dto;

import org.springframework.stereotype.Component;

import com.t1f5.skib.global.dtos.DtoConverter;
import com.t1f5.skib.question.domain.Question;

@Component
public class QuestionDtoConverter implements DtoConverter<QuestionDto, Question> {

    @Override
    public Question convert(QuestionDto dto) {
        return Question.builder()
                .type(dto.getType())
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

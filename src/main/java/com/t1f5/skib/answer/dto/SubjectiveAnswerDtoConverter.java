package com.t1f5.skib.answer.dto;

import com.t1f5.skib.answer.domain.SubjectiveAnswer;
import com.t1f5.skib.global.dtos.DtoConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SubjectiveAnswerDtoConverter
    implements DtoConverter<SubjectiveAnswer, ResponseSubjectiveAnswerDto> {

  @Override
  public ResponseSubjectiveAnswerDto convert(SubjectiveAnswer entity) {
    if (entity == null) {
      return null;
    }

    return ResponseSubjectiveAnswerDto.builder()
        .userAnswerId(entity.getUserAnswerId())
        .questionId(entity.getQuestionId())
        .score(entity.getScore())
        .build();
  }
}

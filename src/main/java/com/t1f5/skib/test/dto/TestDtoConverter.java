package com.t1f5.skib.test.dto;

import com.t1f5.skib.global.dtos.DtoConverter;
import com.t1f5.skib.test.domain.Test;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TestDtoConverter implements DtoConverter<Test, ResponseTestDto> {

  @Override
  public ResponseTestDto convert(Test test) {
    if (test == null) {
      return null;
    }

    return ResponseTestDto.builder()
        .testId(test.getTestId())
        .name(test.getName())
        .limitedTime(test.getLimitedTime())
        .createdAt(test.getCreatedDate())
        .build();
  }
}

package com.t1f5.skib.test.dto;


import com.t1f5.skib.global.dtos.DtoConverter;
import com.t1f5.skib.test.domain.Test;

import lombok.RequiredArgsConstructor;

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
                .createdAt(test.getCreatedDate())
                .build();
    }
    
}

package com.t1f5.skib.test.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class ResponseTestListDto {
     private int count;
    private List<ResponseTestDto> projects;
}

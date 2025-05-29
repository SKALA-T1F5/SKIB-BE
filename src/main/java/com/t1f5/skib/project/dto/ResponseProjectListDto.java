package com.t1f5.skib.project.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class ResponseProjectListDto {
    private int count;
    private List<ResponseProjectDto> projects;
}

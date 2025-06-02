package com.t1f5.skib.document.dto.responsedto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ResponseDocumentListDto {
    private int count;
    private List<ResponseDocumentDto> documents; 
}

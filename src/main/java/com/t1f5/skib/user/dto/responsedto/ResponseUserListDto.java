package com.t1f5.skib.user.dto.responsedto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class ResponseUserListDto {
    private int count;
    private List<ResponseUserDto> users;
}  

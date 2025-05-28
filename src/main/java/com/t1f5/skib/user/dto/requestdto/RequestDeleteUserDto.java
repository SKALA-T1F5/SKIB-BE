// admin이 유저 단일 삭제시 사용하는 DTO
package com.t1f5.skib.user.dto.requestdto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class RequestDeleteUserDto {
    private Integer userId;
}

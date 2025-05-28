// user(trainer, trainee) 정보 수정 요청 DTO
package com.t1f5.skib.user.dto.requestdto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class RequestUpdateUserDto {
    private Integer userId;

    @NotBlank(message = "이름은 공백일 수 없습니다.")
    private String name;

    @NotBlank(message = "부서는 공백일 수 없습니다.")
    private String department;

    @NotBlank(message = "비밀번호는 공백일 수 없습니다.")
    private String password;
}
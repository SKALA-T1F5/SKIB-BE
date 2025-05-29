package com.t1f5.skib.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestCreateAdminDto {
    @NotBlank(message = "아이디는 공백일 수 없습니다.")
    private String id;

    @NotBlank(message = "비밀번호는 공백일 수 없습니다.")
    private String password;
}

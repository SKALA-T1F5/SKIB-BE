// user 생성
package com.t1f5.skib.user.dto.requestdto;

import com.t1f5.skib.user.model.UserType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RequestCreateUserDto {
    @NotEmpty(message = "이메일 리스트는 비어 있을 수 없습니다.")
    private List<String> emails; // 여러 이메일을 한 번에 받기

    @NotBlank(message = "비밀번호는 공백일 수 없습니다.")
    private String password;

    @NotNull(message = "유저 타입은 필수입니다.")
    private UserType type; // TRAINER or TRAINEE
}

// 예시 요청
// {
//     "emails": [
//       "trainer1@example.com",
//       "trainer2@example.com"
//     ],
//     "password": "secure1234",
//     "type": "TRAINER"
//   }

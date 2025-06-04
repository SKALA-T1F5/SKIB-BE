// user 생성
package com.t1f5.skib.user.dto.requestdto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestCreateUserDto {
  @NotEmpty(message = "이메일 리스트는 비어 있을 수 없습니다.")
  private List<String> emails; // 여러 이메일을 한 번에 받기

  @NotBlank(message = "부서는 공백일 수 없습니다.")
  private String department;

  @NotBlank(message = "비밀번호는 공백일 수 없습니다.")
  private String password;
}

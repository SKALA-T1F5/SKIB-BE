// user(trainer, trainee) 정보 수정 요청 DTO
package com.t1f5.skib.user.dto.requestdto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestUpdateUserDto {

  @NotBlank(message = "이름은 공백일 수 없습니다.")
  private String name;

  private String password;
}

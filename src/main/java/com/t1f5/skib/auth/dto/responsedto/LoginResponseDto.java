package com.t1f5.skib.auth.dto.responsedto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponseDto {
  private Integer userId;
  private String token;
  private String role;
  private String name;
}

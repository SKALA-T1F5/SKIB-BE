package com.t1f5.skib.user.dto.responsedto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResponseUserListDto {
  private int count;
  private List<ResponseUserDto> users;
}

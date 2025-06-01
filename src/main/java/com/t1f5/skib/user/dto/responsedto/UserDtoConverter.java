package com.t1f5.skib.user.dto.responsedto;

import com.t1f5.skib.global.dtos.DtoConverter;
import com.t1f5.skib.user.model.User;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class UserDtoConverter implements DtoConverter<User, ResponseUserDto> {
  // 단일 유저를 조회할 때 사용하는 DTO 변환기
  @Override
  public ResponseUserDto convert(User user) {
    if (user == null) {
      return null;
    }

    return ResponseUserDto.builder()
        .userId(user.getUserId())
        .email(user.getEmail())
        .name(user.getName())
        .department(user.getDepartment())
        .type(user.getType())
        .createdAt(user.getCreatedDate().toString())
        .build();
  }
}

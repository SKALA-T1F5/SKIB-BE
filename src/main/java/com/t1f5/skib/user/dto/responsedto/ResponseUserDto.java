package com.t1f5.skib.user.dto.responsedto;

import com.t1f5.skib.global.enums.UserType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseUserDto {
    private Integer userId;
    private String email;
    private String name;
    private String department;
    private UserType type;
    private String createdAt;
}

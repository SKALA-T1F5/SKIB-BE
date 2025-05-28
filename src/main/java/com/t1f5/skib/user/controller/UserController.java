package com.t1f5.skib.user.controller;

import com.t1f5.skib.user.dto.requestdto.RequestCreateUserDto;
import com.t1f5.skib.user.dto.requestdto.RequestUpdateUserDto;
import com.t1f5.skib.user.dto.requestdto.RequestDeleteUserDto;
import com.t1f5.skib.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 1. 유저 생성 (여러 명을 한꺼번에)
    @PostMapping
    public ResponseEntity<?> createUsers(@Valid @RequestBody RequestCreateUserDto dto) {
        userService.createUsers(dto);
        return ResponseEntity.ok("유저 생성 완료");
    }

    // 2. 유저 정보 수정
    @PutMapping("/update")
    public ResponseEntity<?> updateUser(@Valid @RequestBody RequestUpdateUserDto dto) {
        userService.updateUser(dto);
        return ResponseEntity.ok("유저 정보 수정 완료");
    }

    // 3. 유저 삭제
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteUser(@Valid @RequestBody RequestDeleteUserDto dto) {
        userService.deleteUser(dto);
        return ResponseEntity.ok("유제 삭제 완료");
    }
}

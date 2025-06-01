package com.t1f5.skib.user.controller;

import com.t1f5.skib.global.customAnnotations.SwaggerApiNotFoundError;
import com.t1f5.skib.global.customAnnotations.SwaggerApiSuccess;
import com.t1f5.skib.global.customAnnotations.SwaggerInternetServerError;
import com.t1f5.skib.global.dtos.ResultDto;
import com.t1f5.skib.user.dto.requestdto.RequestCreateUserDto;
import com.t1f5.skib.user.dto.requestdto.RequestUpdateUserDto;
import com.t1f5.skib.user.dto.responsedto.ResponseUserDto;
import com.t1f5.skib.user.dto.responsedto.ResponseUserListDto;
import com.t1f5.skib.user.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@Tag(name = "User API", description = "유저 관련 API")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  // 1. 유저 생성 (여러 명을 한꺼번에)
  @SwaggerApiSuccess(summary = "유저 생성", description = "새로운 유저를 생성합니다.")
  @SwaggerApiNotFoundError
  @SwaggerInternetServerError
  @PostMapping
  public ResponseEntity<?> createUsers(@Valid @RequestBody RequestCreateUserDto dto) {
    userService.createUsers(dto);
    return ResponseEntity.ok(ResultDto.res(HttpStatus.OK, "SUCCESS", "유저 생성 완료"));
  }

  // 2. 유저 정보 수정
  @SwaggerApiSuccess(summary = "유저 정보 수정", description = "특정 유저의 정보를 수정합니다.")
  @SwaggerApiNotFoundError
  @SwaggerInternetServerError
  @PutMapping("/update")
  public ResponseEntity<?> updateUser(
      @Valid @RequestBody RequestUpdateUserDto dto, Integer userId) {
    userService.updateUser(userId, dto);
    return ResponseEntity.ok(ResultDto.res(HttpStatus.OK, "SUCCESS", "유저 정보 수정 완료"));
  }

  // 3. 유저 삭제
  @SwaggerApiSuccess(summary = "유저 삭제", description = "특정 유저를 삭제합니다.")
  @SwaggerApiNotFoundError
  @SwaggerInternetServerError
  @DeleteMapping("/delete")
  public ResponseEntity<?> deleteUser(@Valid @RequestBody Integer userId) {
    userService.deleteUser(userId);
    return ResponseEntity.ok(ResultDto.res(HttpStatus.OK, "SUCCESS", "유저 삭제 완료"));
  }

  // 4. 단일 trainer 조회
  @SwaggerApiSuccess(summary = "단일 Trainer 조회", description = "지정된 ID를 가진 Trainer 정보를 조회합니다.")
  @SwaggerApiNotFoundError
  @SwaggerInternetServerError
  @GetMapping("/trainer")
  public ResponseEntity<?> getOneTrainer(@RequestParam Integer userId) {
    ResponseUserDto result = userService.getOneTrainer(userId);
    return ResponseEntity.ok(ResultDto.res(HttpStatus.OK, "SUCCESS", result));
  }

  // 5. 단일 trainee 조회
  @SwaggerApiSuccess(summary = "단일 Trainee 조회", description = "지정된 ID를 가진 Trainee 정보를 조회합니다.")
  @SwaggerApiNotFoundError
  @SwaggerInternetServerError
  @GetMapping("/trainee")
  public ResponseEntity<?> getOneTrainee(@RequestParam Integer userId) {
    ResponseUserDto result = userService.getOneTrainee(userId);
    return ResponseEntity.ok(ResultDto.res(HttpStatus.OK, "SUCCESS", result));
  }

  // 6. 전체 trainer 조회
  @SwaggerApiSuccess(summary = "전체 Trainer 조회", description = "삭제되지 않은 모든 Trainer 계정을 조회합니다.")
  @SwaggerApiNotFoundError
  @SwaggerInternetServerError
  @GetMapping("/trainers")
  public ResponseEntity<?> getAllTrainers() {
    ResponseUserListDto result = userService.getAllTrainers();
    return ResponseEntity.ok(ResultDto.res(HttpStatus.OK, "SUCCESS", result));
  }

  // 7. 전체 trainee 조회
  @SwaggerApiSuccess(summary = "전체 Trainee 조회", description = "삭제되지 않은 모든 Trainee 계정을 조회합니다.")
  @SwaggerApiNotFoundError
  @SwaggerInternetServerError
  @GetMapping("/trainees")
  public ResponseEntity<?> getAllTrainees() {
    ResponseUserListDto result = userService.getAllTrainees();
    return ResponseEntity.ok(ResultDto.res(HttpStatus.OK, "SUCCESS", result));
  }
}

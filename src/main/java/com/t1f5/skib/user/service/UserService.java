package com.t1f5.skib.user.service;

import com.t1f5.skib.global.dtos.DtoConverter;
import com.t1f5.skib.global.enums.UserType;
import com.t1f5.skib.user.dto.requestdto.RequestCreateUserDto;
import com.t1f5.skib.user.dto.requestdto.RequestUpdateUserDto;
import com.t1f5.skib.user.dto.responsedto.ResponseUserDto;
import com.t1f5.skib.user.dto.responsedto.ResponseUserListDto;
import com.t1f5.skib.user.dto.responsedto.UserDtoConverter;
import com.t1f5.skib.user.exception.UserAlreadyExistsException;
import com.t1f5.skib.user.model.User;
import com.t1f5.skib.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  /**
   * 여러 명의 유저(TRAINER or TRAINEE)를 한 번에 생성
   *
   * @param dto 유저 생성 요청 DTO
   * @param userType 생성할 유저의 타입 (TRAINER or TRAINEE)
   * @throws UserAlreadyExistsException 이메일 중복 시 예외 발생
   */
  public void createUsers(RequestCreateUserDto dto, UserType userType) {
    List<String> emails = dto.getEmails();
    String password = passwordEncoder.encode(dto.getPassword());
    String department = dto.getDepartment();

    for (String email : emails) {
      if (userRepository.existsByEmail(email)) {
        throw new UserAlreadyExistsException(email);
      }

      User user =
          User.builder()
              .email(email)
              .password(password)
              .department(department)
              .type(userType)
              .isDeleted(false)
              .build();

      userRepository.save(user);
    }
  }

  /**
   * 유저 정보 수정
   *
   * @param userId 수정할 유저의 ID
   * @param dto 수정할 유저의 정보가 담긴 DTO
   */
  public void updateUser(Integer userId, RequestUpdateUserDto dto) {
    User user =
        userRepository
            .findByUserIdAndIsDeletedFalse(userId)
            .orElseThrow(() -> new RuntimeException("존재하지 않는 유저입니다."));

    user.setName(dto.getName());

    // null이거나 빈 값이면 암호화도 하지 않고 기존 비밀번호 유지
    if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
      user.setPassword(passwordEncoder.encode(dto.getPassword()));
    }

    userRepository.save(user);
  }

  /**
   * 유저 삭제 (Soft Delete)
   *
   * @param userId 삭제할 유저의 ID
   */
  public void deleteUser(Integer userId) {
    User user =
        userRepository.findById(userId).orElseThrow(() -> new RuntimeException("존재하지 않는 유저입니다."));

    user.setIsDeleted(true);
    userRepository.save(user);
  }

  /**
   * 단일 trainer 조회
   *
   * @param userId 조회할 trainer의 ID
   * @return ResponseUserDto 단일 trainer의 정보가 담긴 DTO
   */
  public ResponseUserDto getOneTrainer(Integer userId) {
    User user =
        userRepository
            .findByUserIdAndTypeAndIsDeletedFalse(userId, UserType.TRAINER)
            .orElseThrow(
                () -> new IllegalArgumentException("Trainer not found with id: " + userId));
    DtoConverter<User, ResponseUserDto> converter = new UserDtoConverter();
    return converter.convert(user);
  }

  /**
   * 단일 trainee 조회
   *
   * @param userId 조회할 trainee의 ID
   * @return ResponseUserDto 단일 trainee의 정보가 담긴 DTO
   */
  public ResponseUserDto getOneTrainee(Integer userId) {
    User user =
        userRepository
            .findByUserIdAndTypeAndIsDeletedFalse(userId, UserType.TRAINEE)
            .orElseThrow(
                () -> new IllegalArgumentException("Trainee not found with id: " + userId));
    DtoConverter<User, ResponseUserDto> converter = new UserDtoConverter();
    return converter.convert(user);
  }

  /**
   * 전체 trainer 조회
   *
   * @return ResponseUserListDto 전체 trainer의 정보가 담긴 DTO
   */
  public ResponseUserListDto getAllTrainers() {
    List<User> trainers = userRepository.findAllByTypeAndIsDeletedFalse(UserType.TRAINER);
    UserDtoConverter converter = new UserDtoConverter();

    List<ResponseUserDto> resultList = trainers.stream().map(converter::convert).toList();

    return new ResponseUserListDto(resultList.size(), resultList);
  }

  /**
   * 전체 trainee 조회
   *
   * @return ResponseUserListDto 전체 trainee의 정보가 담긴 DTO
   */
  public ResponseUserListDto getAllTrainees() {
    List<User> trainees = userRepository.findAllByTypeAndIsDeletedFalse(UserType.TRAINEE);
    UserDtoConverter converter = new UserDtoConverter();

    List<ResponseUserDto> resultList = trainees.stream().map(converter::convert).toList();

    return new ResponseUserListDto(resultList.size(), resultList);
  }
}

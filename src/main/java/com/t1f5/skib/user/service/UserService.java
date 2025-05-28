package com.t1f5.skib.user.service;

import com.t1f5.skib.user.dto.requestdto.RequestCreateUserDto;
import com.t1f5.skib.user.dto.requestdto.RequestUpdateUserDto;
import com.t1f5.skib.user.dto.requestdto.RequestDeleteUserDto;
import com.t1f5.skib.user.model.User;
import com.t1f5.skib.user.model.UserType;
import com.t1f5.skib.user.repository.UserRepository;
import com.t1f5.skib.user.exception.UserAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * 여러 명의 유저(Trainer 또는 Trainee)를 한 번에 생성
     */
    public void createUsers(RequestCreateUserDto dto) {
        List<String> emails = dto.getEmails();
        String password = dto.getPassword();
        UserType type = dto.getType();

        for (String email : emails) {
            // 이메일 중복 여부 체크
            if (userRepository.existsByEmail(email)) {
                throw new UserAlreadyExistsException(email);
            }
            
            // // 예외를 던지지 않고 중복된 이메일은 건너뛰기
            // if (userRepository.existsByEmail(email)) {
            //     System.out.println("중복된 이메일: " + email + ", 생성을 건너뜁니다.");
            //     continue;
            // }

            User user = User.builder()
                    .email(email)
                    .password(password) // TODO: 추후 암호화
                    .type(type)
                    .isDeleted(false)
                    .build();
            userRepository.save(user);
        }
    }

    /**
     * 유저 정보 수정
     */
    public void updateUser(RequestUpdateUserDto dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 유저입니다."));

        user.setName(dto.getName());
        user.setDepartment(dto.getDepartment());
        user.setPassword(dto.getPassword()); // TODO: 추후 암호화

        userRepository.save(user);
    }

    /**
     * 유저 삭제 (Soft Delete)
     */
    public void deleteUser(RequestDeleteUserDto dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 유저입니다."));

        user.setIsDeleted(true);
        userRepository.save(user);
    }
}

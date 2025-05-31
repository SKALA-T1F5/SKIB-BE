package com.t1f5.skib.auth.service;

import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.t1f5.skib.auth.dto.requestdto.UserLoginRequestDto;
import com.t1f5.skib.auth.dto.responsedto.LoginResponseDto;
import com.t1f5.skib.auth.dto.responsedto.LogoutResponseDto;
import com.t1f5.skib.auth.exception.AuthenticationException;
import com.t1f5.skib.auth.util.JwtTokenProvider;
import com.t1f5.skib.user.model.User;
import com.t1f5.skib.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthUserService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    public LoginResponseDto login(UserLoginRequestDto request) {
        User user = userRepository.findByEmailAndIsDeletedFalse(request.getEmail())
                .orElseThrow(() -> new AuthenticationException("존재하지 않는 이메일입니다."));

            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                throw new AuthenticationException("비밀번호가 일치하지 않습니다.");
            }

            String role = user.getType().name();
            String token = jwtTokenProvider.createToken(user.getEmail(), role);

            return LoginResponseDto.builder()
                    .token(token)
                    .role(role)
                    .build();
    }

    public LogoutResponseDto logout(String token) {
        // 유효성 검사
        if (!jwtTokenProvider.validateToken(token)) {
            throw new AuthenticationException("유효하지 않은 토큰입니다.");
        }

        String email = jwtTokenProvider.getIdentifier(token);
        String role = jwtTokenProvider.getRole(token);

        // 실제로 로그아웃 처리(토큰 만료/블랙리스트)는 선택
        return LogoutResponseDto.builder()
                .message("User(" + email + ") with role [" + role + "] logged out.")
                .build();
    }
}
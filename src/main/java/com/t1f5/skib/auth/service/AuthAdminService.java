package com.t1f5.skib.auth.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.t1f5.skib.admin.model.Admin;
import com.t1f5.skib.admin.repository.AdminRepository;
import com.t1f5.skib.auth.dto.requestdto.AdminLoginRequestDto;
import com.t1f5.skib.auth.dto.responsedto.LoginResponseDto;
import com.t1f5.skib.auth.dto.responsedto.LogoutResponseDto;
import com.t1f5.skib.auth.exception.AuthenticationException;
import com.t1f5.skib.auth.util.JwtTokenProvider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthAdminService {
    private final AdminRepository adminRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    public LoginResponseDto login(AdminLoginRequestDto request) {
        Admin admin = adminRepository.findByIdAndIsDeletedFalse(request.getId())
                .orElseThrow(() -> new AuthenticationException("존재하지 않는 관리자입니다."));
    
        if (!passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
            throw new AuthenticationException("비밀번호가 일치하지 않습니다.");
        }

        String token = jwtTokenProvider.createToken(admin.getId().toString(), "ADMIN");

        return LoginResponseDto.builder()
                .token(token)
                .role("ADMIN")
                .build();
    }

    public LogoutResponseDto logout(String token) {
        if (!jwtTokenProvider.validateToken(token)) {
            throw new AuthenticationException("유효하지 않은 토큰입니다.");
        }

        String id = jwtTokenProvider.getIdentifier(token);
        String role = jwtTokenProvider.getRole(token);

        return LogoutResponseDto.builder()
                .message("Admin(" + id + ") with role [" + role + "] logged out.")
                .build();
    }
}
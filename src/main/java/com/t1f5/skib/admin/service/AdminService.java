package com.t1f5.skib.admin.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.t1f5.skib.admin.dto.RequestCreateAdminDto;
import com.t1f5.skib.admin.repository.AdminRepository;
import com.t1f5.skib.admin.model.Admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    public void createAdmin(RequestCreateAdminDto dto) {
        if (adminRepository.existsById(dto.getId())) {
            throw new RuntimeException("이미 존재하는 관리자 ID입니다.");
        }

        Admin admin = Admin.builder()
                .id(dto.getId())
                .password(passwordEncoder.encode(dto.getPassword())) 
                .isDeleted(false)
                .build();
        adminRepository.save(admin);
    }
}

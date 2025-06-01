package com.t1f5.skib.auth.service;

import com.t1f5.skib.admin.model.Admin;
import com.t1f5.skib.admin.repository.AdminRepository;
import com.t1f5.skib.auth.exception.AuthenticationException;
import com.t1f5.skib.auth.model.CustomUserDetails;
import com.t1f5.skib.user.model.User;
import com.t1f5.skib.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService {
  private final UserRepository userRepository;
  private final AdminRepository adminRepository;

  /** JWT에서 identifier(email or id)와 role 값을 기반으로 DB에서 사용자 정보를 조회하고 CustomUserDetails로 반환 */
  public CustomUserDetails loadUserByIdentifier(String identifier, String role) {
    if ("ADMIN".equalsIgnoreCase(role)) {
      Admin admin =
          adminRepository
              .findByIdAndIsDeletedFalse(identifier)
              .orElseThrow(() -> new AuthenticationException("존재하지 않는 관리자입니다."));
      return CustomUserDetails.fromAdmin(admin);
    } else {
      User user =
          userRepository
              .findByEmailAndIsDeletedFalse(identifier)
              .orElseThrow(() -> new AuthenticationException("존재하지 않는 유저입니다."));
      return CustomUserDetails.fromUser(user);
    }
  }
}

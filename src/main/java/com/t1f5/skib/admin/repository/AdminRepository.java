package com.t1f5.skib.admin.repository;

import com.t1f5.skib.admin.model.Admin;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminRepository extends JpaRepository<Admin, Integer> {
  boolean existsById(String id); // 중복 체크용

  // 로그인 ID와 삭제되지 않은 상태 기준으로 관리자 조회
  Optional<Admin> findByIdAndIsDeletedFalse(String id);
}

package com.t1f5.skib.user.repository;

import com.t1f5.skib.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Integer> {

    // 이메일로 유저 조회 (로그인 등에서 사용 가능)
    Optional<User> findByEmail(String email);

    // 이름으로 유저 조회-정확한 일치가 아니라 포함 검색시 사용 (LIKE %keyword%)
    List<User> findByNameContaining(String keyword); 

    // 이메일로 전체 유저 목록 중 삭제되지 않은 것만 조회
    boolean existsByEmail(String email);

    // 이름으로(포함검색) 전체 유저 목록 중 삭제되지 않은 것만 조회
    List<User> findByNameContainingAndIsDeletedFalse(String name);
    
    // soft delete용 예시
    Optional<User> findByUserIdAndIsDeletedFalse(Integer userId);
}


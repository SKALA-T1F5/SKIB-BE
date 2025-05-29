package com.t1f5.skib.admin.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.t1f5.skib.admin.model.Admin;

public interface AdminRepository extends JpaRepository<Admin, Integer> {
    boolean existsById(String id); // 중복 체크용
}

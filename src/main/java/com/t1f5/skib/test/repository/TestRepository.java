package com.t1f5.skib.test.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.t1f5.skib.test.domain.Test;

public interface TestRepository extends JpaRepository<Test, Integer> {
    List<Test> findByProject_ProjectId(Integer projectId);
}

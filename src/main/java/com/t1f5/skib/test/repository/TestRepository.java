package com.t1f5.skib.test.repository;

import com.t1f5.skib.test.domain.Test;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestRepository extends JpaRepository<Test, Integer> {
  List<Test> findByProject_ProjectId(Integer projectId);
}

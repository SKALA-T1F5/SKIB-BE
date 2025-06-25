package com.t1f5.skib.test.repository;

import com.t1f5.skib.test.domain.Test;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TestRepository extends JpaRepository<Test, Integer> {
  List<Test> findByProject_ProjectId(Integer projectId);

  @Query("SELECT t.passScore FROM Test t WHERE t.testId = :testId AND t.isDeleted = false")
  Optional<Integer> findPassScoreByTestId(@Param("testId") Integer testId);
}

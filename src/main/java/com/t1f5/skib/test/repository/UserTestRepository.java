package com.t1f5.skib.test.repository;

import com.t1f5.skib.test.domain.Test;
import com.t1f5.skib.test.domain.UserTest;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserTestRepository extends JpaRepository<UserTest, Integer> {
  Optional<UserTest> findByTest_TestIdAndUser_UserIdAndIsDeletedFalse(
      Integer testId, Integer userId);

  @Query(
"""
  SELECT ut
  FROM UserTest ut
  JOIN FETCH ut.test t
  WHERE ut.user.userId = :userId
    AND ut.isDeleted = false
    AND t.isDeleted = false
""")
  List<UserTest> findAllByUserIdAndTestNotDeleted(@Param("userId") Integer userId);

  @EntityGraph(attributePaths = {"test"})
  Optional<UserTest> findByUser_UserIdAndTest_TestIdAndIsDeletedFalse(
      Integer userId, Integer testId);

  List<UserTest> findAllByTest(Test test);

  List<UserTest> findByTest_TestId(Integer testId);
}

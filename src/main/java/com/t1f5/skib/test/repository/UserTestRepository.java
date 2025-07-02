package com.t1f5.skib.test.repository;

import com.t1f5.skib.test.domain.Test;
import com.t1f5.skib.test.domain.UserTest;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserTestRepository extends JpaRepository<UserTest, Integer> {
  Optional<UserTest> findByTest_TestIdAndUser_UserIdAndIsDeletedFalse(
      Integer testId, Integer userId);

  List<UserTest> findAllByUser_UserIdAndIsDeletedFalse(Integer userId);

  Optional<UserTest> findByUser_UserIdAndTest_TestIdAndIsDeletedFalse(
      Integer userId, Integer testId);

  List<UserTest> findAllByTest(Test test);
}

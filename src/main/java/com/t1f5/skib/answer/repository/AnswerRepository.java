package com.t1f5.skib.answer.repository;

import com.t1f5.skib.answer.domain.Answer;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AnswerRepository extends JpaRepository<Answer, Integer> {
  List<Answer> findByUserTest_UserTestId(Integer userTestId);

  @Query(
      "SELECT a FROM Answer a WHERE a.userTest.user.userId = :userId AND a.userTest.test.testId ="
          + " :testId AND a.isDeleted = false")
  List<Answer> findByUserIdAndTestId(
      @Param("userId") Integer userId, @Param("testId") Integer testId);
}

package com.t1f5.skib.feedback.repository;

import com.t1f5.skib.answer.domain.Answer;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedbackUserAnswerRepository extends JpaRepository<Answer, Integer> {

  @Query(
      "SELECT SUM(CASE WHEN a.isCorrect = true THEN 1 ELSE 0 END), COUNT(a) "
          + "FROM Answer a "
          + "WHERE a.userTest.userTestId = :userTestId AND a.isDeleted = false")
  Object[] getTotalAccuracyRateByUserTestId(@Param("userTestId") Integer userTestId);

  @Query(
      "SELECT a.questionId, a.isCorrect " +
      "FROM Answer a " +
      "WHERE a.userTest.userTestId = :userTestId AND a.isDeleted = false")
  List<Object[]> getAnswersByUserTestId(@Param("userTestId") Integer userTestId);
}
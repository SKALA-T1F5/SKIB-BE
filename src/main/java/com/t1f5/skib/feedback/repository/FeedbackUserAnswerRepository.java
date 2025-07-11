package com.t1f5.skib.feedback.repository;

import com.t1f5.skib.answer.domain.Answer;
import com.t1f5.skib.feedback.dto.projection.AnswerMatrixProjection;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedbackUserAnswerRepository extends JpaRepository<Answer, Integer> {

  @Query(
      "SELECT SUM(CASE WHEN a.isCorrect = true THEN 1 ELSE 0 END), COUNT(a) "
          + "FROM Answer a "
          + "WHERE a.userTest.userTestId = :userTestId")
  Object[] getTotalAccuracyRateByUserTestId(@Param("userTestId") Integer userTestId);

  @Query(
      "SELECT a.questionId, a.isCorrect "
          + "FROM Answer a "
          + "WHERE a.userTest.userTestId = :userTestId AND a.isDeleted = false")
  List<Object[]> getAnswersByUserTestId(@Param("userTestId") Integer userTestId);

  // Repository
  @Query(
"""
    SELECT
        a.userTest.user.userId AS userId,
        a.questionId AS questionId,
        a.isCorrect AS isCorrect
    FROM Answer a
    WHERE a.userTest.test.testId = :testId
    AND a.isDeleted = false
""")
  List<AnswerMatrixProjection> findAnswerMatrixByTestId(@Param("testId") Integer testId);

  List<Answer> findByQuestionIdIn(Set<String> questionIds);

  @Query("SELECT a.questionId, a.isCorrect FROM Answer a WHERE a.userTest.userTestId = :userTestId")
  List<Object[]> findQuestionIdAndIsCorrectByUserTestId(@Param("userTestId") Integer userTestId);

  List<Answer> findByUserTest_UserTestId(Integer userTestId);

  @Query(
"""
    SELECT
        ua.userTest.user.userId AS userId,
        ua.userTest.userTestId AS userTestId,
        tq.questionNumber AS questionNumber,
        ua.isCorrect AS isCorrect
    FROM Answer ua
    JOIN TestQuestion tq
      ON tq.test.testId = ua.userTest.test.testId
     AND tq.questionId = ua.questionId
    WHERE ua.userTest.userTestId IN :userTestIds
""")
  List<AnswerMatrixProjection> findAnswerMatrixByUserTestIds(
      @Param("userTestIds") List<Integer> userTestIds);
}
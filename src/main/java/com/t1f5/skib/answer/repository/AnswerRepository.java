package com.t1f5.skib.answer.repository;

import com.t1f5.skib.answer.domain.Answer;
import com.t1f5.skib.global.enums.AttemptType;
import com.t1f5.skib.test.domain.UserTest;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AnswerRepository extends JpaRepository<Answer, Integer> {
  boolean existsByUserTestAndQuestionId(UserTest userTest, String questionId);

  boolean existsByUserTestAndQuestionIdAndAttemptType(
      UserTest userTest, String questionId, AttemptType attemptType);

  List<Answer> findByUserTest_UserTestId(Integer userTestId);

  List<Answer> findByUserTest_UserTestIdAndAttemptType(Integer userTestId, AttemptType attemptType);

  @Query(
      "SELECT a FROM Answer a WHERE a.userTest.user.userId = :userId AND a.userTest.test.testId ="
          + " :testId AND a.isDeleted = false")
  List<Answer> findByUserIdAndTestId(
      @Param("userId") Integer userId, @Param("testId") Integer testId);

  @Query(
      value =
          """
            SELECT
              ua.question_id AS questionId,
              SUM(CASE WHEN ua.is_correct = TRUE THEN 1 ELSE 0 END) AS correctCount,
              COUNT(*) AS totalCount
            FROM user_answer ua
            JOIN user_test ut ON ua.user_test_id = ut.user_test_id
            WHERE ut.test_id = :testId
              AND ua.is_deleted = FALSE
            GROUP BY ua.question_id
          """,
      nativeQuery = true)
  List<QuestionCorrectRateProjection> findCorrectRatesByTestId(@Param("testId") Integer testId);

  @Query(
      "SELECT a FROM Answer a WHERE a.userTest.userTestId = :userTestId AND a.attemptType ="
          + " 'FIRST'")
  List<Answer> findFirstAttemptAnswers(@Param("userTestId") Integer userTestId);

  List<Answer> findByUserTest(UserTest userTest);

  @Query(
      "SELECT COALESCE(SUM(a.score), 0) FROM Answer a WHERE a.userTest = :userTest AND a.isDeleted"
          + " = false")
  int sumScoreByUserTest(@Param("userTest") UserTest userTest);
}

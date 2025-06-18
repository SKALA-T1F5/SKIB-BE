package com.t1f5.skib.feedback.repository;

import com.t1f5.skib.test.domain.UserTest;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedbackUserTestRepository extends JpaRepository<UserTest, Integer> {

  @Query(
      "SELECT ut.userTestId FROM UserTest ut "
          + "WHERE ut.user.userId = :userId AND ut.test.testId = :testId AND ut.isDeleted = false")
  Integer findUserTestIdByUserIdAndTestId(
      @Param("userId") Integer userId, @Param("testId") Integer testId);

  @Query(
      "SELECT ut.score FROM UserTest ut "
          + "WHERE ut.user.userId = :userId AND ut.test.testId = :testId AND ut.isDeleted = false")
  Optional<Integer> findScoreByUserIdAndTestId(
      @Param("userId") Integer userId, @Param("testId") Integer testId);

  @Query(
      "SELECT ut.score FROM UserTest ut "
          + "WHERE ut.test.testId = :testId AND ut.isDeleted = false")
  List<Integer> findAllScoresByTestId(@Param("testId") Integer testId);

  @Query(
      "SELECT AVG(ut.score) FROM UserTest ut WHERE ut.test.testId = :testId AND ut.isDeleted ="
          + " false")
  Double findAverageScoreByTestId(@Param("testId") Integer testId);

  @Query(
      "SELECT COUNT(ut) FROM UserTest ut WHERE ut.test.testId = :testId AND ut.score >= :passScore"
          + " AND ut.isDeleted = false")
  Integer countPassUsersByTestId(
      @Param("testId") Integer testId, @Param("passScore") Integer passScore);

  @Query(
      "SELECT COUNT(ut) FROM UserTest ut WHERE ut.test.testId = :testId AND ut.isDeleted = false")
  Integer countTotalUsersByTestId(@Param("testId") Integer testId);
}
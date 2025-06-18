package com.t1f5.skib.test.repository;

import com.t1f5.skib.test.domain.Test;
import com.t1f5.skib.test.domain.TestQuestion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestQuestionRepository extends JpaRepository<TestQuestion, Integer> {
  List<TestQuestion> findByTest(Test test);
  List<TestQuestion> findByTest_TestId(Integer testId); // ← testId에 해당하는 문제 목록 조회
}

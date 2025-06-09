package com.t1f5.skib.answer.repository;

import com.t1f5.skib.answer.domain.Answer;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnswerRepository extends JpaRepository<Answer, Integer> {
  List<Answer> findByUserTest_UserTestId(Integer userTestId);
}

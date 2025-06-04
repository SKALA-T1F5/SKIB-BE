package com.t1f5.skib.answer.repository;

import com.t1f5.skib.answer.domain.Answer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnswerRepository extends JpaRepository<Answer, Integer> {}

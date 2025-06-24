package com.t1f5.skib.question.repository;

import com.t1f5.skib.question.domain.DocumentQuestion;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentQuestionRepository extends JpaRepository<DocumentQuestion, Integer> {
  List<DocumentQuestion> findByQuestionKeyIn(Set<String> questionKeys);
}

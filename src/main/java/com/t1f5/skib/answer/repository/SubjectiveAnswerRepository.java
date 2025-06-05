package com.t1f5.skib.answer.repository;

import com.t1f5.skib.answer.domain.SubjectiveAnswer;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SubjectiveAnswerRepository extends MongoRepository<SubjectiveAnswer, String> {
  Optional<SubjectiveAnswer> findByUserAnswerId(String userAnswerId);
}

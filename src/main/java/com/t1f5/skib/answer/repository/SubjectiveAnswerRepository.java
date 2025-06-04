package com.t1f5.skib.answer.repository;

import com.t1f5.skib.answer.domain.SubjectiveAnswer;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SubjectiveAnswerRepository extends MongoRepository<SubjectiveAnswer, String> {}

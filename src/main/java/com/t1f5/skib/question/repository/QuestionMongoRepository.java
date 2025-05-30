package com.t1f5.skib.question.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.t1f5.skib.question.domain.Question;

@Repository
public interface QuestionMongoRepository extends MongoRepository<Question, String> {
}

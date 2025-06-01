package com.t1f5.skib.question.repository;

import com.t1f5.skib.question.domain.Question;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionMongoRepository extends MongoRepository<Question, String> {}

package com.t1f5.skib.feedback.repository;

import java.util.List;
import java.util.Set;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.t1f5.skib.question.domain.Question;

@Repository
public interface FeedbackQuestionMongoRepository extends MongoRepository<Question, String> {

  List<Question> findByIdIn(Set<String> ids);
}

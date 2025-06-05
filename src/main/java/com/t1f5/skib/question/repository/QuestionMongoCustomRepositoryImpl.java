package com.t1f5.skib.question.repository;

import com.t1f5.skib.global.enums.QuestionType;
import com.t1f5.skib.question.domain.Question;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.SampleOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

@Slf4j
@RequiredArgsConstructor
@Repository
public class QuestionMongoCustomRepositoryImpl implements QuestionMongoCustomRepository {

  private final MongoTemplate mongoTemplate;

  @Override
  public List<Question> findRandomQuestionsByTypeAndDocumentId(
      String documentId, QuestionType type, int limit) {
    MatchOperation match =
        Aggregation.match(Criteria.where("documentId").is(documentId).and("type").is(type));
    SampleOperation sample = Aggregation.sample(limit);
    Aggregation aggregation = Aggregation.newAggregation(match, sample);
    AggregationResults<Question> result =
        mongoTemplate.aggregate(aggregation, "QUESTION", Question.class);

    log.info("랜덤 추출 문항 수: {}", result.getMappedResults().size());

    return result.getMappedResults();
  }
}

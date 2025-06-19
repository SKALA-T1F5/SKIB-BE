package com.t1f5.skib.document.repository;

import com.t1f5.skib.document.domain.Summary;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SummaryMongoRepository extends MongoRepository<Summary, String> {
  List<Summary> findByDocumentIdIn(List<Integer> documentIds);
}

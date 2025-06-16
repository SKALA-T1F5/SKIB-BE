package com.t1f5.skib.feedback.repository;

import com.t1f5.skib.document.domain.Document;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedbackDocumentQueryRepository extends JpaRepository<Document, Integer> {

  List<Document> findByDocumentIdIn(Set<Integer> documentIds);
  
}

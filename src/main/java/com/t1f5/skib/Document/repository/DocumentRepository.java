package com.t1f5.skib.document.repository;

import com.t1f5.skib.document.domain.Document;
import com.t1f5.skib.global.enums.DocumentStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Document, Integer> {
  // 특정 프로젝트에 속한 모든 문서를 조회 (is_deleted = false는 @Where로 자동 처리)
  List<Document> findAllByProject_ProjectId(Integer projectId);

  List<Document> findByProject_ProjectIdAndIsDeletedFalse(Integer projectId);

  List<Document> findByProject_ProjectIdAndStatusAndIsDeletedFalse(
      Integer projectId, DocumentStatus status);

  Optional<Document> findByProject_ProjectIdAndNameAndIsDeletedFalse(
      Integer projectId, String name);
}

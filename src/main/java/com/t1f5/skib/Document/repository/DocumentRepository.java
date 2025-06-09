package com.t1f5.skib.document.repository;

import com.t1f5.skib.document.domain.Document;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Document, Integer> {
  // 특정 프로젝트에 속한 모든 문서를 조회 (is_deleted = false는 @Where로 자동 처리)
  List<Document> findAllByProject_ProjectId(Integer projectId);
}

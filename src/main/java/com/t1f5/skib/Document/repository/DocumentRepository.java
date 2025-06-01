package com.t1f5.skib.document.repository;

import com.t1f5.skib.document.domain.Document;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Document, Integer> {}

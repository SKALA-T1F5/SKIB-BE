package com.t1f5.skib.document.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.t1f5.skib.document.domain.Document;

public interface DocumentRepository extends JpaRepository<Document, Integer> {
}

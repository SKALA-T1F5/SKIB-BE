package com.t1f5.skib.Document.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.t1f5.skib.Document.domain.Document;

public interface DocumentRepository extends JpaRepository<Document, Integer> {
}

package com.t1f5.skib.test.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.t1f5.skib.test.domain.TestDocumentConfig;

public interface TestDocumentConfigRepository extends JpaRepository<TestDocumentConfig, Integer> {
   
    
}

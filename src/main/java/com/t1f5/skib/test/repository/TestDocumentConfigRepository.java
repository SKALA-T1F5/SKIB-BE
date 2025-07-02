package com.t1f5.skib.test.repository;

import com.t1f5.skib.test.domain.Test;
import com.t1f5.skib.test.domain.TestDocumentConfig;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestDocumentConfigRepository extends JpaRepository<TestDocumentConfig, Integer> {
  List<TestDocumentConfig> findAllByTest(Test test);
}

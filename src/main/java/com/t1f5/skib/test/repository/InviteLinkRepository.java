package com.t1f5.skib.test.repository;

import com.t1f5.skib.test.domain.InviteLink;
import com.t1f5.skib.test.domain.Test;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InviteLinkRepository extends JpaRepository<InviteLink, Integer> {
  Optional<InviteLink> findByTokenAndIsDeletedFalse(String token);

  Optional<InviteLink> findByTest_TestIdAndIsDeletedFalse(Integer testId);

  List<InviteLink> findAllByTest(Test test);
}

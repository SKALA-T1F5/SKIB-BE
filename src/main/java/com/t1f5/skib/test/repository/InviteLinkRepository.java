package com.t1f5.skib.test.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.t1f5.skib.test.domain.InviteLink;

public interface InviteLinkRepository extends JpaRepository<InviteLink, Integer> {
}

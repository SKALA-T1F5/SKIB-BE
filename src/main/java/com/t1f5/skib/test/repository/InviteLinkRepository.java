package com.t1f5.skib.test.repository;

import com.t1f5.skib.test.domain.InviteLink;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface InviteLinkRepository extends JpaRepository<InviteLink, Integer> {
    Optional<InviteLink> findByTokenAndIsDeletedFalse(String token);
}

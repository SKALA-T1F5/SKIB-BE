package com.t1f5.skib.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.t1f5.skib.project.domain.Project;

public interface ProjectJpaRepository extends JpaRepository<Project, Integer> {
}

package com.t1f5.skib.project.repository;

import com.t1f5.skib.project.domain.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectJpaRepository extends JpaRepository<Project, Integer> {}

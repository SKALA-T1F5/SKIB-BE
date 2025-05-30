package com.t1f5.skib.project.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.t1f5.skib.project.domain.Project;
import com.t1f5.skib.project.domain.ProjectUser;

public interface ProjectTrainerRepository extends JpaRepository<ProjectUser, Integer> {
    List<ProjectUser> findByProjectAndIsDeletedFalse(Project project);
}

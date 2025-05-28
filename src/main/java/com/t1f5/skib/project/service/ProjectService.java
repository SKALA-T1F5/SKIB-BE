package com.t1f5.skib.project.service;

import org.springframework.stereotype.Service;

import com.t1f5.skib.project.domain.Project;
import com.t1f5.skib.project.dto.RequestCreateProjectDto;
import com.t1f5.skib.project.repository.ProjectJpaRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
@Service
public class ProjectService {
    private final ProjectJpaRepository projectJpaRepository;

    public void saveProject(RequestCreateProjectDto requestCreateProjectDto) {
        
        Project project = Project.builder()
                .projectName(requestCreateProjectDto.getProjectName())
                .projectDescription(requestCreateProjectDto.getProjectDescription())
                .isDeleted(false) 
                .build();
        
        projectJpaRepository.save(project);
    

    }
}


package com.t1f5.skib.project.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.t1f5.skib.global.dtos.DtoConverter;
import com.t1f5.skib.project.domain.Project;
import com.t1f5.skib.project.dto.RequestCreateProjectDto;
import com.t1f5.skib.project.dto.ResponseProjectDto;
import com.t1f5.skib.project.dto.ResponseProjectListDto;
import com.t1f5.skib.project.repository.ProjectJpaRepository;
import com.t1f5.skib.project.dto.ProjectDtoConverter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
@Service
public class ProjectService {
    private final ProjectJpaRepository projectJpaRepository;

    /**
     * 프로젝트를 생성하는 메서드
     *
     * @param requestCreateProjectDto 프로젝트 생성 요청 DTO
     */
    public void saveProject(RequestCreateProjectDto requestCreateProjectDto) {
        
        Project project = Project.builder()
                .projectName(requestCreateProjectDto.getProjectName())
                .projectDescription(requestCreateProjectDto.getProjectDescription())
                .isDeleted(false) 
                .build();
        
        projectJpaRepository.save(project);
        log.info("Project created successfully: {}", project.getProjectName());
    }

    /**
     * 단일 프로젝트를 조회하는 메서드
     *
     * @param projectId 조회할 프로젝트의 ID
     * @return ResponseProjectDto 단일 프로젝트 정보 DTO
     */
    public ResponseProjectDto getOneProject(Integer projectId) {
        Project project = projectJpaRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with id: " + projectId));

        DtoConverter<Project, ResponseProjectDto> converter = new ProjectDtoConverter();

        return converter.convert(project);
    }

    /**
     * 모든 프로젝트를 조회하는 메서드
     *
     * @return List<ResponseProjectDto> 모든 프로젝트 정보 DTO 리스트
     */
    public ResponseProjectListDto getAllProjects() {
    List<Project> projects = projectJpaRepository.findAll();
    DtoConverter<Project, ResponseProjectDto> converter = new ProjectDtoConverter();

    List<ResponseProjectDto> resultList = projects.stream()
            .map(converter::convert)
            .toList();

    return new ResponseProjectListDto(resultList.size(), resultList);
    }

    /**
     * 프로젝트를 삭제하는 메서드
     *
     * @param projectId 삭제할 프로젝트의 ID
     */
    public void deleteProject(Integer projectId) {
        Project project = projectJpaRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with id: " + projectId));

        project.setIsDeleted(true);
        projectJpaRepository.save(project);
        log.info("Project deleted successfully: {}", project.getProjectName());
    }
}


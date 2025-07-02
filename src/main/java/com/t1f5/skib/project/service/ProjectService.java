package com.t1f5.skib.project.service;

import com.t1f5.skib.global.dtos.DtoConverter;
import com.t1f5.skib.project.domain.Project;
import com.t1f5.skib.project.domain.ProjectUser;
import com.t1f5.skib.project.dto.ProjectDtoConverter;
import com.t1f5.skib.project.dto.ProjectUserDtoConverter;
import com.t1f5.skib.project.dto.RequestCreateProjectDto;
import com.t1f5.skib.project.dto.ResponseProjectDto;
import com.t1f5.skib.project.dto.ResponseProjectListDto;
import com.t1f5.skib.project.dto.ResponseProjectUserDto;
import com.t1f5.skib.project.repository.ProjectJpaRepository;
import com.t1f5.skib.project.repository.ProjectTrainerRepository;
import com.t1f5.skib.test.service.TestService;
import com.t1f5.skib.user.dto.responsedto.UserDtoConverter;
import com.t1f5.skib.user.model.User;
import com.t1f5.skib.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Slf4j
@Service
public class ProjectService {
  private final ProjectJpaRepository projectJpaRepository;
  private final ProjectTrainerRepository projectTrainerRepository;
  private final UserRepository userRepository;
  private final TestService testService;

  /**
   * 프로젝트를 생성하는 메서드
   *
   * @param requestCreateProjectDto 프로젝트 생성 요청 DTO
   */
  public void saveProject(RequestCreateProjectDto requestCreateProjectDto) {

    Project project =
        Project.builder()
            .projectName(requestCreateProjectDto.getProjectName())
            .projectDescription(requestCreateProjectDto.getProjectDescription())
            .isDeleted(false)
            .build();

    projectJpaRepository.save(project);

    for (String email : requestCreateProjectDto.getTrainerEmails()) {
      User user =
          userRepository
              .findByEmailAndIsDeletedFalse(email)
              .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + email));

      ProjectUser projectUser =
          ProjectUser.builder()
              .type(user.getType()) // TRAINER
              .project(project)
              .user(user)
              .isDeleted(false)
              .build();

      projectTrainerRepository.save(projectUser);
    }

    log.info("Project created successfully: {}", project.getProjectName());
  }

  /**
   * 단일 프로젝트를 조회하는 메서드
   *
   * @param projectId 조회할 프로젝트의 ID
   * @return ResponseProjectDto 단일 프로젝트 정보 DTO
   */
  public ResponseProjectDto getOneProject(Integer projectId) {
    Project project =
        projectJpaRepository
            .findById(projectId)
            .orElseThrow(
                () -> new IllegalArgumentException("Project not found with id: " + projectId));

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

    List<ResponseProjectDto> resultList = projects.stream().map(converter::convert).toList();

    return new ResponseProjectListDto(resultList.size(), resultList);
  }

  /**
   * 프로젝트를 삭제하는 메서드
   *
   * @param projectId 삭제할 프로젝트의 ID
   */
  @Transactional
  public void deleteProject(Integer projectId) {
    Project project =
        projectJpaRepository
            .findById(projectId)
            .orElseThrow(
                () -> new IllegalArgumentException("Project not found with id: " + projectId));

    // 프로젝트 연관 사용자들 soft delete
    if (project.getProjectTrainers() != null) {
      project.getProjectTrainers().forEach(pu -> pu.setIsDeleted(true));
    }

    // 프로젝트 연관 테스트 삭제
    testService.deleteTestsByProjectId(projectId);

    // 프로젝트 자체 soft delete
    project.setIsDeleted(true);
    projectJpaRepository.save(project);

    log.info("Project and associated users marked as deleted: {}", project.getProjectName());
  }

  /**
   * 프로젝트에 속한 트레이너와 수강생을 조회하는 메서드
   *
   * @param projectId 조회할 프로젝트의 ID
   * @return ResponseProjectUserDto 프로젝트 사용자 정보 DTO
   */
  public ResponseProjectUserDto getProjectUsers(Integer projectId) {
    Project project =
        projectJpaRepository
            .findById(projectId)
            .orElseThrow(
                () -> new IllegalArgumentException("Project not found with id: " + projectId));

    List<ProjectUser> projectUsers =
        projectTrainerRepository.findByProjectAndIsDeletedFalse(project);

    // userDtoConverter는 DI 받거나 수동 생성
    UserDtoConverter userDtoConverter = new UserDtoConverter();
    DtoConverter<Project, ResponseProjectUserDto> converter =
        new ProjectUserDtoConverter(projectUsers, userDtoConverter);

    return converter.convert(project);
  }

  /**
   * 특정 유저가 속한 프로젝트 목록을 조회하는 메서드
   *
   * @param userId 조회할 유저의 ID
   * @return ResponseProjectListDto 유저가 속한 프로젝트 목록 DTO
   */
  public ResponseProjectListDto getUserProjectList(Integer userId) {
    List<ProjectUser> projects =
        projectTrainerRepository.findByUser_UserIdAndIsDeletedFalse(userId);

    DtoConverter<Project, ResponseProjectDto> converter = new ProjectDtoConverter();

    List<ResponseProjectDto> resultList =
        projects.stream().map(ProjectUser::getProject).map(converter::convert).toList();

    return new ResponseProjectListDto(resultList.size(), resultList);
  }
}

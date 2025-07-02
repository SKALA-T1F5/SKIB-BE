package com.t1f5.skib.project.controller;

import com.t1f5.skib.global.customAnnotations.SwaggerApiNotFoundError;
import com.t1f5.skib.global.customAnnotations.SwaggerApiSuccess;
import com.t1f5.skib.global.customAnnotations.SwaggerInternetServerError;
import com.t1f5.skib.global.dtos.ResultDto;
import com.t1f5.skib.project.dto.RequestCreateProjectDto;
import com.t1f5.skib.project.dto.ResponseProjectDto;
import com.t1f5.skib.project.dto.ResponseProjectListDto;
import com.t1f5.skib.project.dto.ResponseProjectUserDto;
import com.t1f5.skib.project.service.ProjectService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/project")
@Tag(name = "Project API", description = "프로젝트 관련 API")
@RequiredArgsConstructor
public class ProjectController {
  private final ProjectService projectService;

  @SwaggerApiSuccess(summary = "프로젝트 생성", description = "새로운 프로젝트를 생성합니다.")
  @SwaggerApiNotFoundError
  @SwaggerInternetServerError
  @PostMapping
  public ResponseEntity<?> saveProject(@RequestBody RequestCreateProjectDto dto) {
    projectService.saveProject(dto);
    return ResponseEntity.ok(ResultDto.res(HttpStatus.OK, "SUCCESS", "프로젝트가 성공적으로 생성되었습니다."));
  }

  @SwaggerApiSuccess(summary = "프로젝트 조회", description = "특정 프로젝트의 정보를 조회합니다.")
  @SwaggerApiNotFoundError
  @SwaggerInternetServerError
  @GetMapping("/getProject")
  public ResponseEntity<?> getOneProject(Integer projectId) {
    ResponseProjectDto result = projectService.getOneProject(projectId);
    return ResponseEntity.ok(ResultDto.res(HttpStatus.OK, "SUCCESS", result));
  }

  @SwaggerApiSuccess(summary = "모든 프로젝트 조회", description = "모든 프로젝트의 정보를 조회합니다.")
  @SwaggerApiNotFoundError
  @SwaggerInternetServerError
  @GetMapping("/getAllProjects")
  public ResponseEntity<?> getAllProjects() {
    ResponseProjectListDto result = projectService.getAllProjects();

    return ResponseEntity.ok(ResultDto.res(HttpStatus.OK, "SUCCESS", result));
  }

  @SwaggerApiSuccess(summary = "유저의 프로젝트 목록 조회", description = "특정 유저가 참여한 프로젝트 목록을 조회합니다.")
  @SwaggerApiNotFoundError
  @SwaggerInternetServerError
  @GetMapping("/getUserProjectList")
  public ResponseEntity<ResultDto<ResponseProjectListDto>> getUserProjectList(
      @RequestParam Integer userId) {
    ResponseProjectListDto result = projectService.getUserProjectList(userId);
    return ResponseEntity.ok(ResultDto.res(HttpStatus.OK, "SUCCESS", result));
  }

  @SwaggerApiSuccess(summary = "프로젝트 유저 정보 조회", description = "특정 프로젝트의 트레이너/트레이니 유저 정보를 조회합니다.")
  @SwaggerApiNotFoundError
  @SwaggerInternetServerError
  @GetMapping("/getProjectUsers")
  public ResponseEntity<?> getProjectUsers(@RequestParam Integer projectId) {
    ResponseProjectUserDto result = projectService.getProjectUsers(projectId);
    return ResponseEntity.ok(ResultDto.res(HttpStatus.OK, "SUCCESS", result));
  }

  @SwaggerApiSuccess(summary = "프로젝트 삭제", description = "특정 프로젝트를 삭제합니다.")
  @SwaggerApiNotFoundError
  @SwaggerInternetServerError
  @DeleteMapping("/deleteProject")
  public ResponseEntity<?> deleteProject(@RequestParam("projectId") Integer projectId) {
    projectService.deleteProject(projectId);
    return ResponseEntity.ok(ResultDto.res(HttpStatus.OK, "SUCCESS", "프로젝트가 성공적으로 삭제되었습니다."));
  }
}

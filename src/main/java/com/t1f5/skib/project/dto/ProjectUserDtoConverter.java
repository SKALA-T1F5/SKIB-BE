package com.t1f5.skib.project.dto;

import java.util.ArrayList;
import java.util.List;

import com.t1f5.skib.global.dtos.DtoConverter;
import com.t1f5.skib.global.enums.UserType;
import com.t1f5.skib.project.domain.Project;
import com.t1f5.skib.project.domain.ProjectUser;
import com.t1f5.skib.user.dto.responsedto.ResponseUserDto;
import com.t1f5.skib.user.dto.responsedto.UserDtoConverter;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ProjectUserDtoConverter implements DtoConverter<Project, ResponseProjectUserDto> {

    private final List<ProjectUser> projectUsers;
    private final UserDtoConverter userDtoConverter;

    @Override
    public ResponseProjectUserDto convert(Project project) {
        List<ResponseUserDto> trainers = new ArrayList<>();
        List<ResponseUserDto> trainees = new ArrayList<>();

        for (ProjectUser pu : projectUsers) {
            ResponseUserDto userDto = userDtoConverter.convert(pu.getUser());

            if (pu.getType() == UserType.TRAINER) {
                trainers.add(userDto);
            } else if (pu.getType() == UserType.TRAINEE) {
                trainees.add(userDto);
            }
        }

        return ResponseProjectUserDto.builder()
                .projectId(project.getProjectId())
                .projectName(project.getProjectName())
                .projectDescription(project.getProjectDescription())
                .createdAt(project.getCreatedDate().toString())
                .trainer(trainers)
                .trainee(trainees)
                .build();
    }
}


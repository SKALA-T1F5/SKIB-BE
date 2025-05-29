package com.t1f5.skib.project.domain;

import java.util.List;

import org.hibernate.annotations.Where;

import com.t1f5.skib.document.domain.Document;
import com.t1f5.skib.global.domain.BaseTimeEntity;
import com.t1f5.skib.test.domain.Test;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Where(clause = "is_deleted = false")
@Table(name = "PROJECT")
public class Project extends BaseTimeEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer projectId;

    @Column(name = "project_name", nullable = false, length = 40)
    private String projectName;

    @Column(name = "project_description", nullable = false, length = 200)
    private String projectDescription;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted;

    @OneToMany(mappedBy = "project", orphanRemoval = true)
    private List<Project_Trainer> projectTrainers;

    @OneToMany(mappedBy = "project", orphanRemoval = true)
    private List<Test> tests;

    @OneToMany(mappedBy = "project", orphanRemoval = true)
    private List<Document> documents;
}

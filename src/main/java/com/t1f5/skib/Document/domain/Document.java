package com.t1f5.skib.document.domain;

import java.util.List;

import org.hibernate.annotations.Where;

import com.t1f5.skib.global.domain.BaseTimeEntity;
import com.t1f5.skib.project.domain.Project;
import com.t1f5.skib.test.domain.TestDocumentConfig;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "DOCUMENT")
public class Document extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer documentId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "url", nullable = false, length = 500)
    private String url;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "extension", nullable = false, length = 10)
    private String extension;

    @Column(name = "is_uploaded", nullable = false)
    private Boolean isUploaded;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted;

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TestDocumentConfig> testConfigs;
}

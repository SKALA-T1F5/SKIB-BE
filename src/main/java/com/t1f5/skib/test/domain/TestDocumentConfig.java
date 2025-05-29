package com.t1f5.skib.test.domain;

import org.hibernate.annotations.Where;

import com.t1f5.skib.Document.domain.Document;
import com.t1f5.skib.global.domain.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "TEST_DOCUMENT_CONFIG")
public class TestDocumentConfig extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer testDocumentConfigId;

    @Column(name = "configured_objective_count", nullable = false)
    private int configuredObjectiveCount;

    @Column(name = "configured_subjective_count", nullable = false)
    private int configuredSubjectiveCount;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted;

    @ManyToOne
    @JoinColumn(name = "test_id", nullable = false)
    private Test test;

    @ManyToOne
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;
    
}

package com.t1f5.skib.question.domain;

import com.t1f5.skib.document.domain.Document;
import com.t1f5.skib.global.domain.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "DOCUMENT_QUESTION")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentQuestion extends BaseTimeEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer documentQuestionId;

  @Lob
  @Column(name = "question_key", columnDefinition = "TEXT", nullable = false)
  private String questionKey;

  @Column(name = "configured_objective_count", nullable = false)
  private Integer configuredObjectiveCount;

  @Column(name = "configured_subjective_count", nullable = false)
  private Integer configuredSubjectiveCount;

  @Column(name = "is_deleted", nullable = false)
  private Boolean isDeleted;

  @ManyToOne
  @JoinColumn(name = "document_id", nullable = false)
  private Document document;
}

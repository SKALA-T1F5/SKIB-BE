package com.t1f5.skib.question.domain;

import com.t1f5.skib.document.domain.Document;
import com.t1f5.skib.global.domain.BaseTimeEntity;
import com.t1f5.skib.global.enums.QuestionType;
import jakarta.persistence.*;
import lombok.*;

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

  @Column(name = "question_key", nullable = false, length = 100)
  private String questionKey;

  @Enumerated(EnumType.STRING)
  @Column(name = "question_type", nullable = false, length = 10)
  private QuestionType questionType;

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

package com.t1f5.skib.test.domain;

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
import org.hibernate.annotations.Where;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Where(clause = "is_deleted = false")
@Table(name = "TEST_QUESTION")
public class TestQuestion extends BaseTimeEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer testQuestionId;

  @Column(name = "question_number", nullable = true)
  private Integer questionNumber;

  @Column(name = "is_deleted", nullable = false)
  private Boolean isDeleted;

  @ManyToOne
  @JoinColumn(name = "test_id", nullable = false)
  private Test test;

  @Column(name = "question_id", nullable = false, length = 100)
  private String questionId; // MongoDB question _id
}

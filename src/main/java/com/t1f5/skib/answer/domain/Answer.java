package com.t1f5.skib.answer.domain;

import com.t1f5.skib.global.domain.BaseTimeEntity;
import com.t1f5.skib.global.enums.QuestionType;
import com.t1f5.skib.test.domain.UserTest;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
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
@Table(name = "USER_ANSWER")
public class Answer extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "user_answer_id")
  private Integer userAnswerId;

  @Column(name = "question_id", nullable = false, length = 100)
  private String questionId; // MongoDB question _id

  @Column(name = "response", nullable = true, length = 255)
  private String response;

  @Column(name = "is_correct", nullable = false)
  private Boolean isCorrect;

  @Column(name = "score", nullable = true)
  private Integer score;

  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false)
  private QuestionType type;

  @Column(name = "is_deleted", nullable = false)
  private Boolean isDeleted;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_test_id", nullable = false)
  private UserTest userTest;
}

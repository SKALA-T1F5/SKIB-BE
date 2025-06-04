package com.t1f5.skib.answer.domain;

import com.t1f5.skib.global.domain.BaseTimeEntity;
import com.t1f5.skib.global.enums.QuestionType;
import com.t1f5.skib.test.domain.UserTest;
import jakarta.persistence.*;
import lombok.*;
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

  @Column(name = "response", nullable = false, length = 255)
  private String response;

  @Column(name = "is_correct", nullable = false)
  private Boolean isCorrect;

  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false)
  private QuestionType type;

  @Column(name = "is_deleted", nullable = false)
  private Boolean isDeleted;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_test_id", nullable = false)
  private UserTest userTest;
}

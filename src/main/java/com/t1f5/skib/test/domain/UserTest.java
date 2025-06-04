package com.t1f5.skib.test.domain;

import com.t1f5.skib.global.domain.BaseTimeEntity;
import com.t1f5.skib.user.model.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
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
@Table(name = "USER_TEST")
public class UserTest extends BaseTimeEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer userTestId;

  @Column(name = "is_passed", nullable = false)
  private Boolean isPassed;

  @Column(name = "is_taken", nullable = false)
  private Boolean isTaken;

  @Column(name = "retake", nullable = false)
  private Boolean retake;

  @Column(name = "taken_date", nullable = false)
  private LocalDateTime takenDate;

  @Column(name = "score", nullable = false)
  private Integer score;

  @Column(name = "is_deleted", nullable = false)
  private Boolean isDeleted;

  @ManyToOne
  @JoinColumn(name = "test_id", nullable = false)
  private Test test;

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user;
}

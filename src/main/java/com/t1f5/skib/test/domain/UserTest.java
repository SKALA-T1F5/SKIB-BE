package com.t1f5.skib.test.domain;

import java.time.LocalDateTime;

import org.hibernate.annotations.Where;

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
@Table(name = "USER_TEST")
public class UserTest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userTestId;

    @Column(name = "is_passed", nullable = false)
    private Boolean isPassed;

    @Column(name = "is_taken", nullable = false)
    private Boolean isTaken;

    @Column(name = "is_retaken", nullable = false)
    private Boolean isRetaken;

    @Column(name = "taken_date", nullable = false)
    private LocalDateTime takenDate;

    @Column(name = "score", nullable = false)
    private Integer score;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted;

    @ManyToOne
    @JoinColumn(name = "test_id", nullable = false)
    private Test test;

    // 유저랑 연동 추가
}

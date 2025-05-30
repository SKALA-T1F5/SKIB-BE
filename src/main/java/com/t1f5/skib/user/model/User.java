package com.t1f5.skib.user.model;

import java.util.List;

import com.t1f5.skib.global.domain.BaseTimeEntity;
import com.t1f5.skib.global.enums.UserType;
import com.t1f5.skib.project.domain.ProjectUser;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "USER")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId;

    @Column(length = 40, nullable = false, unique = true)
    private String email;

    @Column(length = 120, nullable = false)
    private String password;

    @Column(length = 20)
    private String name;

    @Column(length = 40)
    private String department;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserType type; // TRAINER / TRAINEE

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProjectUser> projectTrainers;

    // // 🔗 관계 설정

    // // ProjectTrainer: 유저가 트레이너로 속한 프로젝트들
    // @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    // private List<ProjectTrainer> projectTrainers = new ArrayList<>();

    // // UserTest: 유저가 응시한 테스트들
    // @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    // private List<UserTest> userTests = new ArrayList<>();

    // // UserAnswer: 유저가 작성한 답변들
    // @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    // private List<UserAnswer> userAnswers = new ArrayList<>();
}

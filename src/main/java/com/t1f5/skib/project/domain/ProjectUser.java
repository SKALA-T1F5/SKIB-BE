package com.t1f5.skib.project.domain;

import com.t1f5.skib.global.domain.BaseTimeEntity;
import com.t1f5.skib.global.enums.UserType;
import com.t1f5.skib.user.model.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "PROJECT_USER")
public class ProjectUser extends BaseTimeEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer projectUserId;

  @Column(name = "is_deleted", nullable = false)
  private Boolean isDeleted;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private UserType type;

  @ManyToOne
  @JoinColumn(name = "project_id", nullable = false)
  private Project project;

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user;
}

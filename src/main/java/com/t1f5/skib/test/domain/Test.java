package com.t1f5.skib.test.domain;

import com.t1f5.skib.global.domain.BaseTimeEntity;
import com.t1f5.skib.global.enums.DifficultyLevel;
import com.t1f5.skib.global.enums.TestStatus;
import com.t1f5.skib.project.domain.Project;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.List;
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
@Table(name = "TEST")
public class Test extends BaseTimeEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer testId;

  @Column(name = "name", nullable = false, length = 500)
  private String name;

  @Lob
  @Column(name = "summary", nullable = false, columnDefinition = "TEXT")
  private String summary;

  @Enumerated(EnumType.STRING)
  @Column(name = "difficulty_level", nullable = false)
  private DifficultyLevel difficultyLevel;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 50)
  private TestStatus status;

  @Column(name = "limited_time_m", nullable = false)
  private Integer limitedTime;

  @Column(name = "pass_score", nullable = false)
  private Integer passScore;

  @Column(name = "question_ids", length = 1000)
  private String questionIds; // "test_id1,test_id4,test_id6" 과 같은 형태로 저장

  @Column(name = "is_retake", nullable = false)
  private Boolean isRetake;

  @Column(name = "is_deleted", nullable = false)
  private Boolean isDeleted;

  @ManyToOne
  @JoinColumn(name = "project_id", nullable = false)
  private Project project;

  @OneToMany(mappedBy = "test", orphanRemoval = true)
  private List<UserTest> userTests;

  @OneToMany(mappedBy = "test", orphanRemoval = true)
  private List<TestDocumentConfig> testDocumentConfigs;

  @OneToMany(mappedBy = "test", orphanRemoval = true)
  private List<InviteLink> inviteLinks;
}

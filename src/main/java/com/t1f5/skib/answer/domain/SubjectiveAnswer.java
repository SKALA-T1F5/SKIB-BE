package com.t1f5.skib.answer.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "SUBJECTIVE_ANSWER_RESULT")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubjectiveAnswer {
  @Id private String id;

  private String userAnswerId;
  private String questionId;
  private String response;
  private int score;
}

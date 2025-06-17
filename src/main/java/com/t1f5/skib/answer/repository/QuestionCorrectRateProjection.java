package com.t1f5.skib.answer.repository;

public interface QuestionCorrectRateProjection {
  String getQuestionId();

  Long getCorrectCount();

  Long getTotalCount();
}

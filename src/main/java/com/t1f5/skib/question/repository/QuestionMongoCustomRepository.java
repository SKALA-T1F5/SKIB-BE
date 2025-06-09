package com.t1f5.skib.question.repository;

import com.t1f5.skib.global.enums.QuestionType;
import com.t1f5.skib.question.domain.Question;
import java.util.List;

public interface QuestionMongoCustomRepository {
  List<Question> findRandomQuestionsByTypeAndDocumentId(
      String documentId, QuestionType type, int limit, Integer projectId);
}

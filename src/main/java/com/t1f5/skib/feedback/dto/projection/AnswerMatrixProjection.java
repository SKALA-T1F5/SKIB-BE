package com.t1f5.skib.feedback.dto.projection;
public interface AnswerMatrixProjection {
    Integer getUserId();
    Integer getUserTestId();
    Integer getQuestionNumber();
    Boolean getIsCorrect();
}

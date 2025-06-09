package com.t1f5.skib.question.dto;

import lombok.Data;
import java.util.List;

@Data
public class QuestionValueDto {
    private String question;
    private List<String> options;
    private String answer;
    private List<String> grading_criteria;
    private String explanation;
}

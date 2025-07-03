package com.t1f5.skib.test.dto;

import com.t1f5.skib.global.services.TranslationService;
import com.t1f5.skib.question.dto.GradingCriteriaDto;
import com.t1f5.skib.question.dto.QuestionDto;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class QuestionTranslator {

  @Autowired private TranslationService translationService;

  public QuestionDto translateQuestionDto(QuestionDto original, String targetLang) {
    return QuestionDto.builder()
        .id(targetLang.equals("ko") ? original.getId() : null) // ID는 번역하지 않음
        .type(original.getType())
        .difficulty_level(original.getDifficulty_level())
        // ✅ 번역 필드
        .question(translateText(original.getQuestion(), targetLang))
        .options(
            original.getOptions() != null
                ? original.getOptions().stream()
                    .map(opt -> translateText(opt, targetLang))
                    .collect(Collectors.toList())
                : null)
        // ✅ 나머지 필드는 그대로 유지
        .answer(original.getAnswer())
        .explanation(original.getExplanation())
        .grading_criteria(original.getGrading_criteria())
        .documentName(
            targetLang.equals("ko") ? original.getDocumentName() : null) // 문서 이름은 한국어로만 유지
        .keywords(original.getKeywords())
        .documentId(original.getDocumentId())
        .tags(original.getTags())
        .build();
  }

  public QuestionDto translateAllQuestionDto(QuestionDto original, String targetLang) {
    return QuestionDto.builder()
        .id(targetLang.equals("ko") ? original.getId() : null) // ID는 번역하지 않음
        .type(original.getType())
        .difficulty_level(original.getDifficulty_level())
        .question(translateText(original.getQuestion(), targetLang))
        .options(
            original.getOptions() != null
                ? original.getOptions().stream()
                    .map(opt -> translateText(opt, targetLang))
                    .collect(Collectors.toList())
                : null)
        .answer(
            original.getAnswer() != null ? translateText(original.getAnswer(), targetLang) : null)
        .explanation(
            original.getExplanation() != null
                ? translateText(original.getExplanation(), targetLang)
                : null)
        .grading_criteria(
            original.getGrading_criteria() != null
                ? original.getGrading_criteria().stream()
                    .map(
                        c -> {
                          GradingCriteriaDto.GradingCriteriaDtoBuilder builder =
                              GradingCriteriaDto.builder();
                          return builder
                              .score(c.getScore())
                              .criteria(translateText(c.getCriteria(), targetLang))
                              .example(translateText(c.getExample(), targetLang))
                              .note(translateText(c.getNote(), targetLang))
                              .build();
                        })
                    .collect(Collectors.toList())
                : null)
        .documentId(original.getDocumentId())
        .documentName(
            targetLang.equals("ko")
                ? original.getDocumentName()
                : translateText(original.getDocumentName(), targetLang))
        .keywords(
            original.getKeywords() != null
                ? original.getKeywords().stream()
                    .map(keyword -> translateText(keyword, targetLang))
                    .collect(Collectors.toList())
                : null)
        .tags(original.getTags())
        .build();
  }

  private String translateText(String text, String targetLang) {
    if (text == null || text.isBlank() || "ko".equalsIgnoreCase(targetLang)) {
      return text;
    }
    return translationService.translate(text, targetLang); // 🔧 실제 번역 서비스 호출
  }
}
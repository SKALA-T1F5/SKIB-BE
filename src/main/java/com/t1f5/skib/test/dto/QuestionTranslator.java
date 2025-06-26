package com.t1f5.skib.test.dto;

import com.t1f5.skib.global.services.TranslationService;
import com.t1f5.skib.question.dto.QuestionDto;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class QuestionTranslator {

  @Autowired private TranslationService translationService;

  public QuestionDto translateQuestionDto(QuestionDto original, String targetLang) {
    return QuestionDto.builder()
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
        .documentId(original.getDocumentId())
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
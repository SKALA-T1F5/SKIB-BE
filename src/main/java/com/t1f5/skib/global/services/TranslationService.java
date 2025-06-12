package com.t1f5.skib.global.services;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TranslationService {

  @Value("${google.translate.credentials-path}")
  private String credentialsPath;

  private Translate translate;

  @PostConstruct
  public void init() {
    try {
      log.info("Initializing Google Translate service with credentials path: {}", credentialsPath);

      GoogleCredentials credentials;

      // 경로가 절대경로인지 상대경로인지 확인
      if (credentialsPath.startsWith("/") || credentialsPath.contains(":")) {
        // 절대 경로
        log.info("Loading credentials from absolute path: {}", credentialsPath);
        credentials = GoogleCredentials.fromStream(new FileInputStream(credentialsPath));
      } else {
        // 클래스패스 리소스
        log.info("Loading credentials from classpath: {}", credentialsPath);
        Resource resource = new ClassPathResource(credentialsPath);
        if (!resource.exists()) {
          throw new RuntimeException("Service account key file not found at: " + credentialsPath);
        }
        try (InputStream inputStream = resource.getInputStream()) {
          credentials = GoogleCredentials.fromStream(inputStream);
        }
      }

      this.translate =
          TranslateOptions.newBuilder().setCredentials(credentials).build().getService();

      log.info("Google Translate service initialized successfully");
    } catch (IOException e) {
      log.error("Failed to load Google credentials from path: {}", credentialsPath, e);
      throw new RuntimeException("Translation service initialization failed: " + e.getMessage(), e);
    } catch (Exception e) {
      log.error("Unexpected error during translation service initialization", e);
      throw new RuntimeException("Translation service initialization failed", e);
    }
  }

  public String translate(String text, String targetLang) {
    try {
      if (translate == null) {
        throw new RuntimeException("Translation service not initialized");
      }

      if (text == null || text.trim().isEmpty()) {
        log.warn("Empty text provided for translation");
        return "";
      }

      log.debug("Translating text to language: {}", targetLang);

      Translation translation =
          translate.translate(text, Translate.TranslateOption.targetLanguage(targetLang));

      String result = translation.getTranslatedText();
      log.debug("Translation completed successfully");

      return result;
    } catch (Exception e) {
      log.error("Translation failed for text: '{}' to language: {}", text, targetLang, e);
      throw new RuntimeException("Translation failed: " + e.getMessage(), e);
    }
  }

  public boolean isServiceAvailable() {
    return translate != null;
  }
}
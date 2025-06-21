package com.t1f5.skib.document.service;

import com.t1f5.skib.document.domain.Document;
import com.t1f5.skib.document.domain.Summary;
import com.t1f5.skib.document.dto.SummaryDto;
import com.t1f5.skib.document.dto.SummaryDtoConverter;
import com.t1f5.skib.document.dto.responsedto.DocumentDtoConverter;
import com.t1f5.skib.document.dto.responsedto.ResponseDocumentDto;
import com.t1f5.skib.document.dto.responsedto.ResponseDocumentListDto;
import com.t1f5.skib.document.repository.DocumentRepository;
import com.t1f5.skib.document.repository.SummaryMongoRepository;
import com.t1f5.skib.global.dtos.DtoConverter;
import com.t1f5.skib.project.domain.Project;
import com.t1f5.skib.project.repository.ProjectJpaRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@RequiredArgsConstructor
@Service
public class DocumentService {
  private final WebClient webClient;
  private final DocumentRepository documentRepository;
  private final ProjectJpaRepository projectRepository;
  private final SummaryMongoRepository summaryMongoRepository;
  private final SummaryDtoConverter summaryDtoConverter;

  /**
   * 문서 ID로 단일 문서를 조회하는 메서드
   *
   * @param documentId 조회할 문서 ID
   * @return 단일 문서 DTO
   */
  @Transactional
  public ResponseDocumentDto getOneDocument(Integer documentId) {
    Document document =
        documentRepository
            .findById(documentId)
            .orElseThrow(
                () -> new IllegalArgumentException("Document not found with id: " + documentId));

    DtoConverter<Document, ResponseDocumentDto> converter = new DocumentDtoConverter();

    return converter.convert(document);
  }

  /**
   * 프로젝트 ID에 해당하는 모든 문서를 조회하는 메서드
   *
   * @param projectId 프로젝트 ID
   * @return 문서 리스트 DTO
   */
  @Transactional
  public ResponseDocumentListDto getDocumentsByProject(Integer projectId) {
    List<Document> documents = documentRepository.findAllByProject_ProjectId(projectId);

    DtoConverter<Document, ResponseDocumentDto> converter = new DocumentDtoConverter();

    List<ResponseDocumentDto> resultList =
        documents.stream().map(converter::convert).collect(Collectors.toList());

    return ResponseDocumentListDto.builder().count(resultList.size()).documents(resultList).build();
  }

  /**
   * 문서를 삭제하는 메서드 (soft delete)
   *
   * @param documentId 삭제할 문서 ID
   */
  @Transactional
  public void deleteDocument(Integer documentId) {
    Document document =
        documentRepository
            .findById(documentId)
            .orElseThrow(
                () -> new IllegalArgumentException("Document not found with id: " + documentId));

    document.setIsDeleted(true);
    documentRepository.save(document);
    log.info("Document deleted: {}", document.getName());
  }

  /**
   * 문서를 저장하는 메서드 1. 프로젝트 조회 2. Document 먼저 저장 (isUploaded = false) 3. FastAPI로 파일 업로드 (documentId
   * 포함) 4. URL 업데이트 및 isUploaded=true
   *
   * @param projectId 프로젝트 ID
   * @param file 업로드할 파일
   */
  @Transactional
  public void saveDocument(Integer projectId, MultipartFile file) {
    // 1. 프로젝트 조회
    Project project =
        projectRepository
            .findById(projectId)
            .orElseThrow(
                () -> new IllegalArgumentException("Project not found with id: " + projectId));

    // 2. Document 먼저 저장 (isUploaded = false)
    Document document =
        Document.builder()
            .name(removeExtension(file.getOriginalFilename()))
            .url(null)
            .fileSize(file.getSize())
            .extension(getExtension(file.getOriginalFilename()))
            .isUploaded(false)
            .isDeleted(false)
            .project(project)
            .build();

    documentRepository.save(document); // 여기서 documentId 생성됨

    // 3. FastAPI로 파일 업로드 (documentId 포함)
    String uploadedUrl =
        sendFileToFastAPI(file, projectId, document.getDocumentId(), file.getOriginalFilename());

    // 4. URL 업데이트 및 isUploaded=true
    document.setUrl(uploadedUrl);
    document.setIsUploaded(true);
    documentRepository.save(document);

    log.info(
        "Document saved successfully: name='{}', size={} bytes, extension='{}', projectId={},"
            + " url={}",
        document.getName(),
        document.getFileSize(),
        document.getExtension(),
        projectId,
        document.getUrl());
  }

  /**
   * FastAPI로 파일을 업로드하고, SummaryDto[]를 요청하여 MongoDB에 저장하는 메서드
   *
   * @param file 업로드할 파일
   * @param projectId 프로젝트 ID
   * @param documentId 문서 ID
   * @param name 파일 이름
   * @return 업로드된 파일의 경로
   */
  private String sendFileToFastAPI(
      MultipartFile file, Integer projectId, Integer documentId, String name) {
    try {
      // 1. FastAPI로 파일 업로드
      MultiValueMap<String, Object> multipartData = new LinkedMultiValueMap<>();
      multipartData.add("file", file.getResource());
      multipartData.add("project_id", projectId.toString());
      multipartData.add("document_id", documentId.toString());
      multipartData.add("name", name);

      Map<String, Object> response =
          webClient
              .post()
              .uri("http://skib-ai.skala25a.project.skala-ai.com:8000/api/document")
              .contentType(MediaType.MULTIPART_FORM_DATA)
              .body(BodyInserters.fromMultipartData(multipartData))
              .retrieve()
              .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
              .block(); // 여기까지 완료되어야 아래 실행됨

      log.info("FastAPI 업로드 완료: {}", response);

      // 업로드 응답에서 file_path 또는 필요한 값 추출
      String filePath = (String) response.get("file_path");
      if (filePath == null) {
        throw new IllegalStateException("file_path가 응답에 없습니다.");
      }

      waitForFastAPIProcessing(documentId);

      // 2. SummaryDto[] 요청 및 저장
      SummaryDto[] summaries =
          webClient
              .get()
              .uri(
                  "http://skib-ai.skala25a.project.skala-ai.com:8000/api/document/summary/"
                      + documentId)
              .retrieve()
              .bodyToMono(SummaryDto[].class)
              .block();

      if (summaries != null && summaries.length > 0) {
        for (SummaryDto summaryDto : summaries) {
          Summary summary = summaryDtoConverter.convert(summaryDto, documentId);
          summaryMongoRepository.save(summary);
        }
      }

      return filePath;

    } catch (Exception e) {
      log.error("FastAPI 연동 실패: {}", e.getMessage(), e);
      throw new RuntimeException("FastAPI 연동 실패", e);
    }
  }

  private String getExtension(String filename) {
    int dotIndex = filename.lastIndexOf('.');
    return (dotIndex != -1) ? filename.substring(dotIndex + 1) : "";
  }

  private String removeExtension(String filename) {
    if (filename == null) return null;
    int lastDotIndex = filename.lastIndexOf(".");
    if (lastDotIndex == -1) return filename; // 확장자 없음
    return filename.substring(0, lastDotIndex);
  }

  private void waitForFastAPIProcessing(Integer documentId) throws InterruptedException {
    int retries = 20;
    while (retries-- > 0) {
      Map<String, String> statusResponse =
          webClient
              .get()
              .uri("http://skib-ai.skala25a.project.skala-ai.com/api/document/status/" + documentId)
              .retrieve()
              .bodyToMono(new ParameterizedTypeReference<Map<String, String>>() {})
              .block();

      String status = statusResponse.get("status");
      log.info("현재 상태: {}", status);

      if ("완료되었습니다".equals(status)) {
        return;
      } else if ("실패하였습니다".equals(status)) {
        throw new IllegalStateException("FastAPI 문서 전처리 실패");
      }

      Thread.sleep(3000); // 3초 간격으로 polling
    }

    throw new IllegalStateException("FastAPI 전처리 시간 초과");
  }
}

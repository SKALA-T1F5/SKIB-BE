package com.t1f5.skib.document.service;

import com.t1f5.skib.document.domain.Document;
import com.t1f5.skib.document.domain.Summary;
import com.t1f5.skib.document.dto.SummaryDto;
import com.t1f5.skib.document.dto.SummaryDtoConverter;
import com.t1f5.skib.document.dto.SummaryNotification;
import com.t1f5.skib.document.dto.responsedto.DocumentDtoConverter;
import com.t1f5.skib.document.dto.responsedto.ResponseDocumentDto;
import com.t1f5.skib.document.dto.responsedto.ResponseDocumentListDto;
import com.t1f5.skib.document.exception.DuplicateDocumentNameException;
import com.t1f5.skib.document.repository.DocumentRepository;
import com.t1f5.skib.document.repository.SummaryMongoRepository;
import com.t1f5.skib.global.dtos.DtoConverter;
import com.t1f5.skib.global.enums.DocumentStatus;
import com.t1f5.skib.project.domain.Project;
import com.t1f5.skib.project.repository.ProjectJpaRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
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

  @Value("${fastapi.base-url}")
  private String fastApiBaseUrl;

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
  public Integer saveDocument(Integer projectId, MultipartFile file) {
    // 1. 프로젝트 조회
    Project project =
        projectRepository
            .findById(projectId)
            .orElseThrow(
                () -> new IllegalArgumentException("Project not found with id: " + projectId));

    // 문서 중복 체크 코드
    String documentName = removeExtension(file.getOriginalFilename());

    Optional<Document> existingDocument =
        documentRepository.findByProject_ProjectIdAndNameAndIsDeletedFalse(projectId, documentName);

    if (existingDocument.isPresent()) {
      log.info(
          "⚠️ 동일한 이름의 문서가 이미 존재합니다. 저장을 건너뜁니다. documentId: {}",
          existingDocument.get().getDocumentId());
      throw new DuplicateDocumentNameException(documentName);
    }

    // 2. Document 먼저 저장 (isUploaded = false)
    Document document =
        Document.builder()
            .name(removeExtension(file.getOriginalFilename()))
            .url(null)
            .fileSize(file.getSize())
            .extension(getExtension(file.getOriginalFilename()))
            .isUploaded(false)
            .isDeleted(false)
            .status(DocumentStatus.UPLOAD_COMPLETED) // 초기 상태는 UPLOAD_COMPLETED로 설정
            .project(project)
            .build();

    documentRepository.save(document); // 여기서 documentId 생성됨

    log.info("Document saved with ID: {}", document.getDocumentId());
    log.info(
        "Document details: name='{}', size={} bytes, extension='{}', projectId={}",
        document.getName(),
        document.getDocumentId(),
        file.getOriginalFilename(),
        projectId);

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

    return document.getDocumentId();
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
      MultipartBodyBuilder builder = new MultipartBodyBuilder();

      // ⚠️ file은 MultipartFile에서 직접 바이트 배열로 받거나 Resource로 전환
      builder.part("file", file.getResource());
      builder.part("document_id", documentId.toString());
      builder.part("project_id", projectId.toString());
      builder.part("name", name);

      Map<String, Object> response =
          webClient
              .post()
              .uri(fastApiBaseUrl + "api/document/upload")
              .contentType(MediaType.MULTIPART_FORM_DATA)
              .body(BodyInserters.fromMultipartData(builder.build()))
              .retrieve()
              .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
              .block();

      log.info("FastAPI 업로드 완료: {}", response);

      String filePath = (String) response.get("file_path");
      if (filePath == null) {
        throw new IllegalStateException("file_path가 응답에 없습니다.");
      }

      return filePath;

    } catch (Exception e) {
      log.error("FastAPI 연동 실패: {}", e.getMessage(), e);
      throw new RuntimeException("FastAPI 연동 실패", e);
    }
  }

  /**
   * 문서 상태를 업데이트하는 메서드
   *
   * @param documentId 문서 ID
   * @param status 업데이트할 상태
   */
  @Transactional
  public void updateDocumentStatus(Integer documentId, DocumentStatus status) {
    Document document =
        documentRepository
            .findById(documentId)
            .orElseThrow(() -> new IllegalArgumentException("문서를 찾을 수 없습니다: " + documentId));
    document.setStatus(status);
    documentRepository.save(document);
  }

  /**
   * 문서의 현재 상태(status)를 반환하는 메서드
   *
   * @param documentId 문서 ID
   * @return 문서 상태
   */
  @Transactional
  public DocumentStatus getDocumentStatus(Integer documentId) {
    Document document =
        documentRepository
            .findById(documentId)
            .orElseThrow(() -> new IllegalArgumentException("문서를 찾을 수 없습니다: " + documentId));
    return document.getStatus();
  }

  /**
   * FastAPI로부터 SummaryDto를 받아 MongoDB에 저장하는 메서드
   *
   * @param documentId 문서 ID
   * @param summaryDto SummaryDto 객체
   */
  @Transactional
  public void saveSummaryFromFastAPI(Integer documentId, SummaryDto summaryDto) {
    Summary summary = summaryDtoConverter.convert(summaryDto, documentId);
    summaryMongoRepository.save(summary);

    log.info("🚀 Saving summary: {}", summary);
  }

  /**
   * 문서 상태를 저장하는 메서드
   *
   * @param documentId 문서 ID
   * @param status 업데이트할 상태
   */
  @Transactional
  public void saveDocumentStatus(SummaryNotification summaryNotification) {
    updateDocumentStatus(summaryNotification.getDocumentId(), summaryNotification.getStatus());
    log.info(
        "Document status updated: documentId={}, status={}",
        summaryNotification.getDocumentId(),
        summaryNotification.getStatus());
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
}

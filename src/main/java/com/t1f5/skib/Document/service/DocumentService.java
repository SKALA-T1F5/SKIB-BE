package com.t1f5.skib.document.service;

import com.t1f5.skib.document.domain.Document;
import com.t1f5.skib.document.dto.requestdto.RequestCreateDocumentDto;
import com.t1f5.skib.document.dto.responsedto.DocumentDtoConverter;
import com.t1f5.skib.document.dto.responsedto.ResponseDocumentDto;
import com.t1f5.skib.document.dto.responsedto.ResponseDocumentListDto;
import com.t1f5.skib.document.repository.DocumentRepository;
import com.t1f5.skib.global.dtos.DtoConverter;
import com.t1f5.skib.project.domain.Project;
import com.t1f5.skib.project.repository.ProjectJpaRepository;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class DocumentService {
  private final DocumentRepository documentRepository;
  private final ProjectJpaRepository projectRepository;

  /**
   * 문서를 저장하는 메서드
   *
   * @param projectId 해당 문서가 속할 프로젝트 ID
   * @param dto 문서 생성 요청 DTO
   */
  @Transactional
  public void saveDocument(Integer projectId, RequestCreateDocumentDto dto) {
    Project project =
        projectRepository
            .findById(projectId)
            .orElseThrow(
                () -> new IllegalArgumentException("Project not found with id: " + projectId));

    Document document =
        Document.builder()
            .name(dto.getName())
            .url(dto.getUrl())
            .fileSize(dto.getFileSize())
            .extension(dto.getExtension())
            .isUploaded(dto.getIsUploaded())
            .isDeleted(false)
            .project(project)
            .build();

    documentRepository.save(document);
    log.info("Document created successfully: {} (projectId: {})", document.getName(), projectId);
  }

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

    return ResponseDocumentListDto.builder()
        .count(resultList.size())
        .documents(resultList)
        .build();
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
}

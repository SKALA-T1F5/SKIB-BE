package com.t1f5.skib.document.controller;

import com.t1f5.skib.document.dto.SummaryDto;
import com.t1f5.skib.document.dto.SummaryNotification;
import com.t1f5.skib.document.dto.responsedto.ResponseDocumentDto;
import com.t1f5.skib.document.dto.responsedto.ResponseDocumentListDto;
import com.t1f5.skib.document.service.DocumentService;
import com.t1f5.skib.global.customAnnotations.SwaggerApiNotFoundError;
import com.t1f5.skib.global.customAnnotations.SwaggerApiSuccess;
import com.t1f5.skib.global.customAnnotations.SwaggerInternetServerError;
import com.t1f5.skib.global.dtos.ResultDto;
import com.t1f5.skib.global.enums.DocumentStatus;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Slf4j
@Tag(name = "Document API", description = "문서 관련 API")
@RequiredArgsConstructor
public class DocumentController {

  private final DocumentService documentService;

  @SwaggerApiSuccess(summary = "문서 업로드 후 정보 저장", description = "PDF 파일과 문서 정보를 업로드한 후 저장합니다.")
  @SwaggerApiNotFoundError
  @SwaggerInternetServerError
  @PostMapping(value = "/api/document", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<?> uploadDocument(
      @RequestParam("projectId") Integer projectId, @RequestPart("file") MultipartFile file) {
    documentService.saveDocument(projectId, file);
    return ResponseEntity.ok(ResultDto.res(HttpStatus.OK, "SUCCESS", "문서 업로드 완료"));
  }

  @SwaggerApiSuccess(summary = "문서 목록 조회", description = "특정 프로젝트에 속한 문서들의 정보를 조회합니다.")
  @SwaggerApiNotFoundError
  @SwaggerInternetServerError
  @GetMapping("/api/documents")
  public ResponseEntity<?> getDocuments(@RequestParam("projectId") Integer projectId) {
    ResponseDocumentListDto documents = documentService.getDocumentsByProject(projectId);
    return ResponseEntity.ok(ResultDto.res(HttpStatus.OK, "SUCCESS", documents));
  }

  @SwaggerApiSuccess(summary = "단일 문서 조회", description = "문서 ID로 문서 정보를 조회합니다.")
  @SwaggerApiNotFoundError
  @SwaggerInternetServerError
  @GetMapping("/api/document")
  public ResponseEntity<?> getDocument(@RequestParam("documentId") Integer documentId) {
    ResponseDocumentDto document = documentService.getOneDocument(documentId);
    return ResponseEntity.ok(ResultDto.res(HttpStatus.OK, "SUCCESS", document));
  }

  @SwaggerApiSuccess(summary = "문서 삭제", description = "문서 ID로 문서를 삭제합니다.")
  @SwaggerApiNotFoundError
  @SwaggerInternetServerError
  @DeleteMapping("/api/document/delete")
  public ResponseEntity<?> deleteDocument(@RequestParam("documentId") Integer documentId) {
    documentService.deleteDocument(documentId);
    return ResponseEntity.ok(ResultDto.res(HttpStatus.OK, "SUCCESS", "문서 삭제 완료"));
  }

  @SwaggerApiSuccess(summary = "문서 요약 저장", description = "FastAPI로부터 받은 문서 요약 정보를 저장합니다.")
  @SwaggerApiNotFoundError
  @SwaggerInternetServerError
  @PutMapping("/api/document/summary/{documentId}")
  public ResponseEntity<ResultDto<Void>> receiveSummaryFromFastAPI(
      @PathVariable Integer documentId, @RequestBody SummaryDto summaryDto) {
    log.info("📥 수신된 요약 데이터: {}", summaryDto); // 🔥 로그 확인
    documentService.saveSummaryFromFastAPI(documentId, summaryDto);
    return ResponseEntity.ok(ResultDto.res(HttpStatus.OK, "SUCCESS", null));
  }

  @SwaggerApiSuccess(summary = "문서 상태 저장", description = "문서의 현재 상태(status)를 저장합니다.")
  @SwaggerApiNotFoundError
  @SwaggerInternetServerError
  @PutMapping("api/document/progress")
  public ResponseEntity<ResultDto<Void>> saveDocumentStatus(
      @RequestBody SummaryNotification summaryNotification) {
    documentService.saveDocumentStatus(summaryNotification);
    return ResponseEntity.ok(ResultDto.res(HttpStatus.OK, "SUCCESS", null));
  }

  @SwaggerApiSuccess(summary = "문서 상태 조회", description = "문서의 현재 상태(status)를 반환합니다.")
  @GetMapping("/api/document/status")
  public ResponseEntity<ResultDto<DocumentStatus>> getDocumentStatus(
      @RequestParam Integer documentId) {
    DocumentStatus status = documentService.getDocumentStatus(documentId);
    return ResponseEntity.ok(ResultDto.res(HttpStatus.OK, "SUCCESS", status));
  }
}
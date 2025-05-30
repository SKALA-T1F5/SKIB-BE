package com.t1f5.skib.test.service;

import org.springframework.stereotype.Service;

import com.t1f5.skib.document.domain.Document;
import com.t1f5.skib.document.repository.DocumentRepository;
import com.t1f5.skib.global.dtos.DtoConverter;
import com.t1f5.skib.project.repository.ProjectJpaRepository;
import com.t1f5.skib.test.domain.InviteLink;
import com.t1f5.skib.test.domain.Test;
import com.t1f5.skib.test.domain.TestDocumentConfig;
import com.t1f5.skib.test.dto.RequestCreateTestDto;
import com.t1f5.skib.test.dto.ResponseTestDto;
import com.t1f5.skib.test.dto.ResponseTestListDto;
import com.t1f5.skib.test.dto.TestDocumentConfigDto;
import com.t1f5.skib.test.dto.TestDtoConverter;
import com.t1f5.skib.test.repository.InviteLinkRepository;
import com.t1f5.skib.test.repository.TestDocumentConfigRepository;
import com.t1f5.skib.test.repository.TestRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.UUID;
import java.time.LocalDateTime;

@RequiredArgsConstructor
@Slf4j
@Service
public class TestService {
    // 기존 필드
    private final TestRepository testRepository;
    private final DocumentRepository documentRepository; 
    private final ProjectJpaRepository projectRepository;
    private final TestDocumentConfigRepository testDocumentConfigRepository;
    private final InviteLinkRepository inviteLinkRepository; 

    /**
     * 테스트를 저장하고 초대 링크를 생성합니다.
     * @param projectId
     * @param requestCreateTestDto
     * @return
     */
    public String saveTest(Integer projectId, RequestCreateTestDto requestCreateTestDto) {
        log.info("Saving test with name: {}", requestCreateTestDto.getName());

        // 1. 테스트 저장
        Test test = Test.builder()
                .name(requestCreateTestDto.getName())
                .difficultyLevel(requestCreateTestDto.getDifficultyLevel())
                .limitedTime(requestCreateTestDto.getLimitedTime())
                .passScore(requestCreateTestDto.getPassScore())
                .isRetaken(requestCreateTestDto.getIsRetaken())
                .isDeleted(false)
                .project(projectRepository.findById(projectId).orElseThrow())
                .build();
        testRepository.save(test);

        // 2. 문서별 구성 저장
        for (TestDocumentConfigDto configDto : requestCreateTestDto.getDocumentConfigs()) {
            Document document = documentRepository.findById(configDto.getDocumentId())
                    .orElseThrow(() -> new IllegalArgumentException("해당 문서를 찾을 수 없습니다: " + configDto.getDocumentId()));

            TestDocumentConfig config = TestDocumentConfig.builder()
                    .test(test)
                    .document(document)
                    .configuredObjectiveCount(configDto.getConfiguredObjectiveCount())
                    .configuredSubjectiveCount(configDto.getConfiguredSubjectiveCount())
                    .isDeleted(false)
                    .build();
            testDocumentConfigRepository.save(config);
        }

        // 3. 초대 링크 생성 및 저장
        String token = UUID.randomUUID().toString();
        LocalDateTime expiration = LocalDateTime.now().plusDays(7); // 예: 7일 유효

        InviteLink inviteLink = InviteLink.builder()
                .test(test)
                .token(token)
                .expiresAt(expiration)
                .isDeleted(false)
                .build();
        inviteLinkRepository.save(inviteLink);

        // 4. 초대링크 URL 리턴
        return "https://localhost:8080/invite/" + token;
    }

        /**
         * 테스트 ID로 테스트를 조회합니다.
         * @param testId
         * @return
         */
        public ResponseTestDto getTestById(Integer testId) {
            log.info("Fetching test with ID: {}", testId);
            Test test = testRepository.findById(testId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 테스트를 찾을 수 없습니다: " + testId));
                    
            DtoConverter<Test, ResponseTestDto> converter = new TestDtoConverter();
            
            return converter.convert(test);      
        }

        public ResponseTestListDto getAllTests(Integer projectId) {
            log.info("Fetching all tests for project ID: {}", projectId);
            var tests = testRepository.findByProject_ProjectId(projectId);
            
            DtoConverter<Test, ResponseTestDto> converter = new TestDtoConverter();
            
            var resultList = tests.stream()
                    .map(converter::convert)
                    .toList();
                    
            return new ResponseTestListDto(resultList.size(), resultList);
        }

        /**
         * 테스트 ID로 테스트를 삭제합니다.
         * @param testId
         */
        public void deleteTest(Integer testId) {
            log.info("Deleting test with ID: {}", testId);
            Test test = testRepository.findById(testId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 테스트를 찾을 수 없습니다: " + testId));
                    
            test.setIsDeleted(true);
            testRepository.save(test);
            log.info("Test deleted successfully: {}", test.getName());
        }
}

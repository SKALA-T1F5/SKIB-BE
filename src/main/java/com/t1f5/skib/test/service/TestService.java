package com.t1f5.skib.test.service;

import org.springframework.stereotype.Service;

import com.t1f5.skib.document.domain.Document;
import com.t1f5.skib.document.repository.DocumentRepository;
import com.t1f5.skib.project.repository.ProjectJpaRepository;
import com.t1f5.skib.test.domain.Test;
import com.t1f5.skib.test.domain.TestDocumentConfig;
import com.t1f5.skib.test.dto.RequestCreateTestDto;
import com.t1f5.skib.test.dto.TestDocumentConfigDto;
import com.t1f5.skib.test.repository.TestDocumentConfigRepository;
import com.t1f5.skib.test.repository.TestRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class TestService {
    private final TestRepository testRepository;
    private final DocumentRepository documentRepository; 
    private final ProjectJpaRepository projectRepository;
    private final TestDocumentConfigRepository testDocumentConfigRepository;

    public void saveTest(Integer projectId, RequestCreateTestDto requestCreateTestDto) {
        log.info("Saving test with name: {}", requestCreateTestDto.getName());
        
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

        // 각 문서 설정을 기반으로 TestDocumentConfig 생성
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
    }
    
}

package com.t1f5.skib.test.service;

import com.t1f5.skib.document.domain.Document;
import com.t1f5.skib.document.repository.DocumentRepository;
import com.t1f5.skib.global.dtos.DtoConverter;
import com.t1f5.skib.project.repository.ProjectJpaRepository;
import com.t1f5.skib.test.domain.InviteLink;
import com.t1f5.skib.test.domain.Test;
import com.t1f5.skib.test.domain.TestDocumentConfig;
import com.t1f5.skib.test.domain.UserTest;
import com.t1f5.skib.test.dto.RequestCreateTestDto;
import com.t1f5.skib.test.dto.ResponseTestDto;
import com.t1f5.skib.test.dto.ResponseTestListDto;
import com.t1f5.skib.test.dto.TestDocumentConfigDto;
import com.t1f5.skib.test.dto.TestDtoConverter;
import com.t1f5.skib.test.repository.InviteLinkRepository;
import com.t1f5.skib.test.repository.TestDocumentConfigRepository;
import com.t1f5.skib.test.repository.TestRepository;
import com.t1f5.skib.test.repository.UserTestRepository;
import com.t1f5.skib.user.repository.UserRepository;

import java.util.Optional;

import com.t1f5.skib.user.model.User;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
  private final UserRepository userRepository;
  private final UserTestRepository userTestRepository;

  /**
   * 테스트를 저장하고 초대 링크를 생성합니다.
   *
   * @param projectId
   * @param requestCreateTestDto
   * @return
   */
  public String saveTest(Integer projectId, RequestCreateTestDto requestCreateTestDto) {
    log.info("Saving test with name: {}", requestCreateTestDto.getName());

    // 1. 테스트 저장
    Test test =
        Test.builder()
            .name(requestCreateTestDto.getName())
            .difficultyLevel(requestCreateTestDto.getDifficultyLevel())
            .limitedTime(requestCreateTestDto.getLimitedTime())
            .passScore(requestCreateTestDto.getPassScore())
            .isRetake(requestCreateTestDto.getIsRetake())
            .isDeleted(false)
            .project(projectRepository.findById(projectId).orElseThrow())
            .build();
    testRepository.save(test);

    // 2. 문서별 구성 저장
    for (TestDocumentConfigDto configDto : requestCreateTestDto.getDocumentConfigs()) {
      Document document =
          documentRepository
              .findById(configDto.getDocumentId())
              .orElseThrow(
                  () ->
                      new IllegalArgumentException(
                          "해당 문서를 찾을 수 없습니다: " + configDto.getDocumentId()));

      TestDocumentConfig config =
          TestDocumentConfig.builder()
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

    InviteLink inviteLink =
        InviteLink.builder().test(test).token(token).expiresAt(expiration).isDeleted(false).build();
    inviteLinkRepository.save(inviteLink);

    // 4. 초대링크 URL 리턴
    return "https://localhost:8080/invite/" + token;
  }

  /**
   * 테스트 ID로 테스트를 조회합니다.
   *
   * @param userTestId 유저 테스트 ID
   * @return ResponseTestDto
   */
  public ResponseTestDto getTestById(Integer userTestId) {
    log.info("Fetching test with ID: {}", userTestId);
    UserTest userTest =
        userTestRepository
            .findById(userTestId)
            .orElseThrow(() -> new IllegalArgumentException("해당 유저테스트를 찾을 수 없습니다: " + userTestId));

    Test test =
        testRepository
            .findById(userTest.getTest().getTestId())
            .orElseThrow(() -> new IllegalArgumentException("해당 테스트를 찾을 수 없습니다: " + userTest.getTest().getTestId()));

    DtoConverter<Test, ResponseTestDto> converter = new TestDtoConverter();

    return converter.convert(test);
  }

  public ResponseTestListDto getAllTests(Integer projectId) {
    log.info("Fetching all tests for project ID: {}", projectId);
    var tests = testRepository.findByProject_ProjectId(projectId);

    DtoConverter<Test, ResponseTestDto> converter = new TestDtoConverter();

    var resultList = tests.stream().map(converter::convert).toList();

    return new ResponseTestListDto(resultList.size(), resultList);
  }

  /**
   * 테스트 ID로 테스트를 삭제합니다.
   *
   * @param testId
   */
  public void deleteTest(Integer testId) {
    log.info("Deleting test with ID: {}", testId);
    Test test =
        testRepository
            .findById(testId)
            .orElseThrow(() -> new IllegalArgumentException("해당 테스트를 찾을 수 없습니다: " + testId));

    test.setIsDeleted(true);
    testRepository.save(test);
    log.info("Test deleted successfully: {}", test.getName());
  }

  /**
   * 초대 링크와 이메일을 기반으로 유저를 유저테스트에 등록합니다.
   *
   * @param token 초대 토큰
   * @param email 유저 이메일
   */
  public void registerUserToTest(String token, Integer userId) {
    // 1. 초대 토큰으로 InviteLink 찾기
    InviteLink inviteLink =
        inviteLinkRepository
            .findByTokenAndIsDeletedFalse(token)
            .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 초대 토큰입니다."));

    if (inviteLink.getExpiresAt().isBefore(LocalDateTime.now())) {
      throw new IllegalArgumentException("초대 링크가 만료되었습니다.");
    }

    // 2. 해당 테스트
    Test test = inviteLink.getTest();

    // 3. 이메일로 유저 조회
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("해당 유저가 존재하지 않습니다."));

    // 4. 이미 등록된 UserTest 있는지 확인
    Optional<UserTest> existing =
        userTestRepository.findByTest_TestIdAndUser_UserIdAndIsDeletedFalse(
            test.getTestId(), user.getUserId());

    if (existing.isPresent()) {
      log.info("이미 등록된 유저입니다.");
      return;
    }

    // 5. 새로운 UserTest 등록
    UserTest userTest =
        UserTest.builder()
            .test(test)
            .user(user)
            .isTaken(false)
            .retake(false)
            .isPassed(false)
            .takenDate(LocalDateTime.now())
            .score(0)
            .isDeleted(false)
            .build();

    userTestRepository.save(userTest);
  }
}

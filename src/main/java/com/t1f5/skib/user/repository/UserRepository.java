package com.t1f5.skib.user.repository;

import com.t1f5.skib.global.enums.UserType;
import com.t1f5.skib.user.model.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {
  /**
   * 삭제되지 않은 사용자 중, 주어진 ID를 가진 사용자를 조회합니다.
   *
   * @param id 사용자 ID
   * @return Optional<User>
   */
  Optional<User> findByUserIdAndIsDeletedFalse(Integer id);

  /**
   * 삭제되지 않은 특정 타입(UserType)의 사용자 중, 주어진 ID를 가진 사용자를 조회합니다.
   *
   * @param id 사용자 ID
   * @param type 사용자 타입 (TRAINER or TRAINEE)
   * @return Optional<User>
   */
  Optional<User> findByUserIdAndTypeAndIsDeletedFalse(Integer userId, UserType type);

  /**
   * 삭제되지 않은 특정 타입(UserType)의 모든 사용자 리스트를 조회합니다.
   *
   * @param type 사용자 타입 (TRAINER or TRAINEE)
   * @return List<User>
   */
  List<User> findAllByTypeAndIsDeletedFalse(UserType type);

  /**
   * 주어진 이메일을 가진 사용자가 존재하는지 확인합니다.
   *
   * @param email 사용자 이메일
   * @return boolean
   */
  boolean existsByEmail(String email);

  /**
   * 삭제되지 않은 사용자의 이메일로 사용자를 조회합니다.
   *
   * @param email 사용자 이메일
   * @return Optional<User>
   */
  Optional<User> findByEmailAndIsDeletedFalse(String email);

  /**
   * 삭제되지 않은 사용자의 ID로 사용자를 조회합니다.
   *
   * @param userId 사용자 ID
   * @return Optional<User>
   */
  Optional<User> findByIdAndIsDeletedFalse(Integer userId);
}

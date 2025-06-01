package com.t1f5.skib.user.exception;

public class UserAlreadyExistsException extends RuntimeException {
  public UserAlreadyExistsException(String email) {
    super("이미 존재하는 이메일입니다: " + email);
  }
}

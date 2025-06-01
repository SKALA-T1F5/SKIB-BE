package com.t1f5.skib.global.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
@AllArgsConstructor
@Builder
public class ResultDto<T> {
  private HttpStatus statusCode;
  private String resultMsg;
  private T resultData;

  public ResultDto(final HttpStatus statusCode, final String resultMsg) {
    this.statusCode = statusCode;
    this.resultMsg = resultMsg;
    this.resultData = null;
  }

  public static <T> ResultDto<T> res(final HttpStatus statusCode, final String resultMsg) {
    return ResultDto.<T>builder()
        .statusCode(statusCode)
        .resultMsg(resultMsg)
        .resultData(null)
        .build();
  }

  public static <T> ResultDto<T> res(
      final HttpStatus statusCode, final String resultMsg, final T t) {
    return ResultDto.<T>builder().statusCode(statusCode).resultMsg(resultMsg).resultData(t).build();
  }
}

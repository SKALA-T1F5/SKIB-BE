package com.t1f5.skib.global.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.t1f5.skib.global.dtos.ResultDto;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ResultDto<String>> handleIllegalArgument(IllegalArgumentException ex) {
        return new ResponseEntity<>(
                ResultDto.res(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage()),
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResultDto<String>> handleException(Exception ex) {
        ex.printStackTrace(); // 서버 로그 기록
        return new ResponseEntity<>(
                ResultDto.res(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", ex.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}

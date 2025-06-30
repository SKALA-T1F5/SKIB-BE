package com.t1f5.skib.document.exception;

public class DuplicateDocumentNameException extends RuntimeException {
    public DuplicateDocumentNameException(String name) {
        super("동일한 이름의 문서가 이미 존재합니다: " + name);
    }
}

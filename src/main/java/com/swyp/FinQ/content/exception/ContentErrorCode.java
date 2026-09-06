package com.swyp.FinQ.content.exception;

import com.swyp.FinQ.global.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ContentErrorCode implements ErrorCode {

    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "CATEGORY_NOT_FOUND", "해당 카테고리를 찾을 수 없습니다."),
    CONTENT_NOT_FOUND(HttpStatus.NOT_FOUND, "CONTENT_NOT_FOUND", "존재하지 않는 콘텐츠입니다.");

    private final HttpStatus status;
    private final String errorCode;
    private final String message;

    @Override
    public HttpStatus status() {
        return status;
    }

    @Override
    public String errorCode() {
        return errorCode;
    }

    @Override
    public String message() {
        return message;
    }
}

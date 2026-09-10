package com.swyp.FinQ.content.exception;

import com.swyp.FinQ.global.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ContentErrorCode implements ErrorCode {

    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "CATEGORY_NOT_FOUND", "해당 카테고리를 찾을 수 없습니다."),
    CONTENT_NOT_FOUND(HttpStatus.NOT_FOUND, "CONTENT_NOT_FOUND", "존재하지 않는 콘텐츠입니다."),
    PREMIUM_CONTENT_ACCESS_DENIED(HttpStatus.FORBIDDEN, "PREMIUM_CONTENT_ACCESS_DENIED", "프리미엄 콘텐츠는 접근할 수 없습니다."),
    BODY_DATA_PARSE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "BODY_DATA_PARSE_FAILED", "콘텐츠 본문 데이터를 파싱할 수 없습니다.");

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

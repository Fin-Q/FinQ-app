package com.swyp.FinQ.user.exception;

import com.swyp.FinQ.global.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum UserErrorCode implements ErrorCode {

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "사용자를 찾을 수 없습니다."),
    INTEREST_ALREADY_SELECTED(
            HttpStatus.CONFLICT,
            "USER_INTEREST_ALREADY_SELECTED",
            "관심 주제가 이미 등록되어 있습니다."
    ),
    DUPLICATE_INTEREST_CATEGORY(
            HttpStatus.BAD_REQUEST,
            "USER_DUPLICATE_INTEREST_CATEGORY",
            "동일한 관심 주제를 중복해서 선택할 수 없습니다."
    );

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

package com.swyp.FinQ.notification.exception;

import com.swyp.FinQ.global.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum NotificationErrorCode implements ErrorCode {

    PUSH_TOKEN_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "PUSH_TOKEN_NOT_FOUND",
            "등록된 푸시 토큰을 찾을 수 없습니다."
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

package com.swyp.FinQ.user.exception;

import com.swyp.FinQ.global.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum AuthErrorCode implements ErrorCode {

    EMAIL_ALREADY_EXISTS(
            HttpStatus.CONFLICT,
            "AUTH_EMAIL_ALREADY_EXISTS",
            "이미 가입된 이메일입니다."
    ),
    REQUIRED_AGREEMENT_MISSING(
            HttpStatus.BAD_REQUEST,
            "AUTH_REQUIRED_AGREEMENT_MISSING",
            "필수 약관 동의가 누락되었습니다."
    ),
    REQUIRED_AGREEMENT_NOT_ACCEPTED(
            HttpStatus.BAD_REQUEST,
            "AUTH_REQUIRED_AGREEMENT_NOT_ACCEPTED",
            "모든 필수 약관에 동의해야 합니다."
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

package com.swyp.FinQ.streak.exception;

import com.swyp.FinQ.global.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum StreakErrorCode implements ErrorCode {

    MONTH_OUT_OF_RANGE(
            HttpStatus.BAD_REQUEST,
            "STREAK_MONTH_OUT_OF_RANGE",
            "조회할 수 없는 기간입니다."
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

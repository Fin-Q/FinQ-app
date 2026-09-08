package com.swyp.FinQ.streak.success;

import com.swyp.FinQ.global.success.SuccessCode;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum StreakSuccessCode implements SuccessCode {

    STATUS_RETRIEVED(HttpStatus.OK, "스트릭 상태 조회에 성공했습니다."),
    CALENDAR_RETRIEVED(HttpStatus.OK, "월간 스트릭 조회에 성공했습니다.");

    private final HttpStatus status;
    private final String message;

    @Override
    public HttpStatus status() {
        return status;
    }

    @Override
    public String message() {
        return message;
    }
}

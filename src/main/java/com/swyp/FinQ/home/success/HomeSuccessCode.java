package com.swyp.FinQ.home.success;

import com.swyp.FinQ.global.success.SuccessCode;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum HomeSuccessCode implements SuccessCode {

    HOME_RETRIEVED(HttpStatus.OK, "홈 화면 조회에 성공했습니다.");

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
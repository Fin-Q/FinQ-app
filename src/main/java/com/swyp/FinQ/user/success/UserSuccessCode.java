package com.swyp.FinQ.user.success;

import com.swyp.FinQ.global.success.SuccessCode;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum UserSuccessCode implements SuccessCode {

    ONBOARDING_RETRIEVED(HttpStatus.OK, "온보딩 상태 조회에 성공했습니다."),
    INTERESTS_CREATED(HttpStatus.CREATED, "관심 주제 저장에 성공했습니다.");

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

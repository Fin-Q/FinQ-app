package com.swyp.FinQ.user.success;

import com.swyp.FinQ.global.success.SuccessCode;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum AuthSuccessCode implements SuccessCode {

    AGREEMENTS_RETRIEVED(HttpStatus.OK, "약관 목록 조회에 성공했습니다."),
    SIGN_UP(HttpStatus.CREATED, "회원가입에 성공했습니다."),
    LOGIN(HttpStatus.OK, "로그인에 성공했습니다."),
    TOKEN_REFRESH(HttpStatus.OK, "토큰 재발급에 성공했습니다."),
    LOGOUT(HttpStatus.OK, "로그아웃에 성공했습니다.");

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

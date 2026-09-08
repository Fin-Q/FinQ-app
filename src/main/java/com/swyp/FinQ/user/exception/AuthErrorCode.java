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
    ),
    INVALID_CREDENTIALS(
            HttpStatus.UNAUTHORIZED,
            "AUTH_INVALID_CREDENTIALS",
            "이메일 또는 비밀번호가 올바르지 않습니다."
    ),
    INVALID_REFRESH_TOKEN(
            HttpStatus.UNAUTHORIZED,
            "AUTH_INVALID_REFRESH_TOKEN",
            "유효하지 않은 Refresh Token입니다."
    ),
    INVALID_PASSWORD_RESET_REQUEST(
            HttpStatus.BAD_REQUEST,
            "AUTH_INVALID_PASSWORD_RESET_REQUEST",
            "유효하지 않은 비밀번호 재설정 요청입니다."
    ),
    PASSWORD_RESET_RESEND_TOO_EARLY(
            HttpStatus.TOO_MANY_REQUESTS,
            "AUTH_PASSWORD_RESET_RESEND_TOO_EARLY",
            "인증번호 재전송 대기시간이 지나지 않았습니다."
    ),
    PASSWORD_RESET_EMAIL_SEND_FAILED(
            HttpStatus.SERVICE_UNAVAILABLE,
            "AUTH_PASSWORD_RESET_EMAIL_SEND_FAILED",
            "인증번호 이메일 발송에 실패했습니다."
    ),
    INVALID_KAKAO_ACCESS_TOKEN(
            HttpStatus.UNAUTHORIZED,
            "AUTH_INVALID_KAKAO_ACCESS_TOKEN",
            "유효하지 않은 Kakao Access Token입니다."
    ),
    INVALID_KAKAO_SIGN_UP_INFO(
            HttpStatus.BAD_REQUEST,
            "AUTH_INVALID_KAKAO_SIGN_UP_INFO",
            "신규 Kakao 사용자의 닉네임은 필수이며 50자 이하여야 합니다."
    ),
    KAKAO_AUTH_SERVER_UNAVAILABLE(
            HttpStatus.SERVICE_UNAVAILABLE,
            "AUTH_KAKAO_AUTH_SERVER_UNAVAILABLE",
            "Kakao 인증 서버를 일시적으로 사용할 수 없습니다."
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

package com.swyp.FinQ.notification.success;

import com.swyp.FinQ.global.success.SuccessCode;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum NotificationSuccessCode implements SuccessCode {

    PUSH_TOKEN_REGISTERED(HttpStatus.OK, "푸시 토큰이 저장되었습니다."),
    PUSH_TOKEN_UNREGISTERED(HttpStatus.OK, "푸시 토큰 등록이 해제되었습니다."),
    NOTIFICATION_SETTING_UPDATED(HttpStatus.OK, "알림 설정이 변경되었습니다.");

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

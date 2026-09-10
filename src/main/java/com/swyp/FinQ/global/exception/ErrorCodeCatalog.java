package com.swyp.FinQ.global.exception;

import com.swyp.FinQ.notification.exception.NotificationErrorCode;
import com.swyp.FinQ.streak.exception.StreakErrorCode;
import com.swyp.FinQ.user.exception.AuthErrorCode;
import com.swyp.FinQ.user.exception.UserErrorCode;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Swagger와 외부 문서가 참조할 수 있는 FinQ 공통 에러 코드 카탈로그
 */
public final class ErrorCodeCatalog {

    private static final Map<String, ErrorCode> ERROR_CODES = createErrorCodes();

    private ErrorCodeCatalog() {
    }

    public static List<ErrorCode> values() {
        return List.copyOf(ERROR_CODES.values());
    }

    public static Optional<ErrorCode> find(String errorCode) {
        return Optional.ofNullable(ERROR_CODES.get(errorCode));
    }

    public static ErrorCode require(String errorCode) {
        return find(errorCode)
                .orElseThrow(() -> new IllegalArgumentException("등록되지 않은 에러 코드입니다: " + errorCode));
    }

    private static Map<String, ErrorCode> createErrorCodes() {
        Map<String, ErrorCode> errorCodes = new LinkedHashMap<>();
        register(errorCodes, GlobalErrorCode.values());
        register(errorCodes, AuthErrorCode.values());
        register(errorCodes, UserErrorCode.values());
        register(errorCodes, NotificationErrorCode.values());
        register(errorCodes, StreakErrorCode.values());
        return Map.copyOf(errorCodes);
    }

    private static void register(Map<String, ErrorCode> errorCodes, ErrorCode[] candidates) {
        for (ErrorCode candidate : candidates) {
            ErrorCode previous = errorCodes.putIfAbsent(candidate.errorCode(), candidate);
            if (previous != null) {
                throw new IllegalStateException("중복된 에러 코드입니다: " + candidate.errorCode());
            }
        }
    }
}

package com.swyp.FinQ.global.exception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum GlobalErrorCode implements ErrorCode {

  /**
   * 400: 요청 실패 - 클라이언트 오류
   */
  COMMON_VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "COMMON_VALIDATION_ERROR", "요청 데이터가 유효하지 않습니다. 입력값을 확인해 주세요."),
  COMMON_INVALID_REQUEST(HttpStatus.BAD_REQUEST, "COMMON_INVALID_REQUEST", "잘못된 요청입니다. 요청 형식 또는 파라미터를 확인해 주세요."),
  COMMON_RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON_RESOURCE_NOT_FOUND", "요청한 리소스를 찾을 수 없습니다. URI 경로를 확인해 주세요."),
  COMMON_METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "COMMON_METHOD_NOT_ALLOWED", "허용되지 않는 HTTP 메서드입니다. 요청 메서드를 확인해 주세요."),
  COMMON_MISSING_PARAMETER(HttpStatus.BAD_REQUEST, "COMMON_MISSING_PARAMETER", "필수 요청 파라미터가 누락되었습니다."),
  COMMON_INVALID_REQUEST_BODY(HttpStatus.BAD_REQUEST, "COMMON_INVALID_REQUEST_BODY", "요청 바디가 올바르지 않습니다. JSON 형식을 확인해 주세요."),
  COMMON_UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "COMMON_UNSUPPORTED_MEDIA_TYPE", "지원하지 않는 미디어 타입입니다. Content-Type을 확인해 주세요."),

  /**
   * 401, 403: 인증/인가 오류
   */
  AUTH_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH_UNAUTHORIZED", "로그인이 필요한 요청입니다."),
  AUTH_FORBIDDEN(HttpStatus.FORBIDDEN, "AUTH_FORBIDDEN", "요청한 작업을 수행할 권한이 없습니다."),

  /**
   * 500: 응답 실패 - 서버 오류
   */
  COMMON_INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.");

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

package com.swyp.FinQ.global.exception;

import lombok.Getter;


/**
 * 애플리케이션 전반에서 공통으로 사용할 커스텀 런타임 예외 클래스
 */
@Getter
public class BaseException extends RuntimeException {
  private final ErrorCode code;
  private final String customMessage;

  public BaseException(ErrorCode code) {
    super(code.message());
    this.code = code;
    this.customMessage = null;
  }

  public BaseException(ErrorCode code, String customMessage) {
    super(customMessage);
    this.code = code;
    this.customMessage = customMessage;
  }

  public BaseException(ErrorCode code, Throwable cause) {
    super(code.message(), cause);
    this.code = code;
    this.customMessage = null;
  }

  public BaseException(ErrorCode code, String customMessage, Throwable cause) {
    super(customMessage, cause);
    this.code = code;
    this.customMessage = customMessage;
  }

  public static BaseException of(ErrorCode code) {
    return new BaseException(code);
  }

  public static BaseException of(ErrorCode code, String customMessage) {
    return new BaseException(code, customMessage);
  }
}

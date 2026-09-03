package com.swyp.FinQ.global.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  /**
   * Spring Security 인증 실패 (401 Unauthorized)
   */
  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ErrorResponse> onAuthenticationException(AuthenticationException e) {
    log.warn("Authentication failed: {}", e.getMessage());
    return ErrorResponse.of(GlobalErrorCode.AUTH_UNAUTHORIZED);
  }

  /**
   * 우리 서비스에서 직접 던지는 BaseException 처리
   */
  @ExceptionHandler(BaseException.class)
  public ResponseEntity<ErrorResponse> onBaseException(BaseException e) {
    ErrorCode code = e.getCode();
    log.warn("Business error: {} | {}", code.errorCode(), code.message(), e);
    String message = e.getCustomMessage() != null ? e.getCustomMessage() : code.message();
    return ErrorResponse.of(code, message);
  }

  /**
   * @RequestBody 바인딩/검증 에러 처리
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> onMethodArgumentNotValid(MethodArgumentNotValidException e) {
    List<ErrorResponse.FieldDetail> details = e.getBindingResult().getFieldErrors().stream()
      .map(fe -> new ErrorResponse.FieldDetail(fe.getField(), fe.getDefaultMessage()))
      .toList();
    log.info("Validation failed: {}", details);
    return ErrorResponse.of(GlobalErrorCode.COMMON_VALIDATION_ERROR, GlobalErrorCode.COMMON_VALIDATION_ERROR.message(), details);
  }

  /**
   * 필수 @RequestParam 누락 처리
   */
  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<ErrorResponse> onMissingParam(MissingServletRequestParameterException e) {
    log.info("Missing parameter: {}", e.getParameterName());
    List<ErrorResponse.FieldDetail> details = List.of(
      new ErrorResponse.FieldDetail(e.getParameterName(), "MISSING")
    );
    return ErrorResponse.of(GlobalErrorCode.COMMON_MISSING_PARAMETER,
      "필수 파라미터 '" + e.getParameterName() + "'이(가) 누락되었습니다.", details);
  }

  /**
   * @Validated 제약 조건 위반 처리 (@PathVariable, @RequestParam 검증)
   */
  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ErrorResponse> onConstraintViolation(ConstraintViolationException e) {
    List<ErrorResponse.FieldDetail> details = e.getConstraintViolations().stream()
      .map(v -> {
        String propertyPath = v.getPropertyPath().toString();
        String field = propertyPath.contains(".")
          ? propertyPath.substring(propertyPath.lastIndexOf('.') + 1)
          : propertyPath;
        return new ErrorResponse.FieldDetail(field, v.getMessage());
      })
      .toList();
    log.info("Constraint violation: {}", details);
    return ErrorResponse.of(GlobalErrorCode.COMMON_VALIDATION_ERROR, GlobalErrorCode.COMMON_VALIDATION_ERROR.message(), details);
  }

  /**
   * 잘못된 JSON (파싱) 에러 처리
   */
  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponse> onHttpMessageNotReadable(HttpMessageNotReadableException e) {
    log.info("Malformed JSON request: {}", e.getMessage());
    return ErrorResponse.of(GlobalErrorCode.COMMON_INVALID_REQUEST_BODY);
  }

  /**
   * 존재하지 않는 URL 또는 파라미터 타입 불일치
   */
  @ExceptionHandler({NoHandlerFoundException.class, MethodArgumentTypeMismatchException.class})
  public ResponseEntity<ErrorResponse> onNotFoundOrTypeMismatch(Exception e) {
    log.info("Not found or type mismatch: {}", e.getMessage());
    return ErrorResponse.of(GlobalErrorCode.COMMON_RESOURCE_NOT_FOUND);
  }

  /**
   * 잘못된 HTTP 메서드 (GET → POST 등)
   */
  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<ErrorResponse> onMethodNotSupported(HttpRequestMethodNotSupportedException e) {
    log.info("Method not supported: {}", e.getMethod());
    return ErrorResponse.of(GlobalErrorCode.COMMON_METHOD_NOT_ALLOWED);
  }

  /**
   * 잘못된 Media Type (Content-Type)
   */
  @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
  public ResponseEntity<ErrorResponse> onMediaTypeNotSupported(HttpMediaTypeNotSupportedException e) {
    log.info("Media type not supported: {}", e.getContentType());
    return ErrorResponse.of(GlobalErrorCode.COMMON_UNSUPPORTED_MEDIA_TYPE);
  }

  /**
   * 그 외 모든 예외 처리 (500 Internal Server Error)
   */
  @ExceptionHandler(Throwable.class)
  public ResponseEntity<ErrorResponse> onAnyException(Throwable e) {
    log.error("Unhandled exception", e);
    return ErrorResponse.of(GlobalErrorCode.COMMON_INTERNAL_SERVER_ERROR);
  }
}

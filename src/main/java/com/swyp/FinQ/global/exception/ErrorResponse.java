package com.swyp.FinQ.global.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
  String status,
  String errorCode,
  String message,
  List<FieldDetail> details,
  String traceId
) {

  public record FieldDetail(
    String field,
    String reason
  ) {}

  public static ResponseEntity<ErrorResponse> of(ErrorCode code) {
    return ResponseEntity
      .status(code.status())
      .body(from(code));
  }

  public static ResponseEntity<ErrorResponse> of(ErrorCode code, String message) {
    return ResponseEntity
      .status(code.status())
      .body(from(code, message));
  }

  public static ResponseEntity<ErrorResponse> of(ErrorCode code, String message, List<FieldDetail> details) {
    return ResponseEntity
      .status(code.status())
      .body(new ErrorResponse(
        "ERROR",
        code.errorCode(),
        message,
        details,
        generateTraceId()
      ));
  }

  public static ErrorResponse from(ErrorCode code) {
    return from(code, code.message());
  }

  public static ErrorResponse from(ErrorCode code, String message) {
    return new ErrorResponse(
      "ERROR",
      code.errorCode(),
      message,
      List.of(),
      generateTraceId()
    );
  }

  private static String generateTraceId() {
    return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
  }
}

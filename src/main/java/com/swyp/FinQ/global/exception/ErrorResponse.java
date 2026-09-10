package com.swyp.FinQ.global.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "API 오류 응답")
public record ErrorResponse(
  @Schema(description = "요청 처리 상태", example = "ERROR", allowableValues = "ERROR")
  String status,
  @Schema(description = "클라이언트 분기 처리용 오류 코드", example = "COMMON-001")
  String errorCode,
  @Schema(description = "사용자 또는 개발자가 확인할 오류 메시지", example = "요청 값이 올바르지 않습니다.")
  String message,
  @Schema(description = "필드별 검증 오류 목록. 검증 오류가 아니면 빈 배열")
  List<FieldDetail> details,
  @Schema(description = "서버 로그 추적용 식별자", example = "a1b2c3d4e5f67890")
  String traceId
) {

  @Schema(description = "필드 검증 오류 상세")
  public record FieldDetail(
    @Schema(description = "오류가 발생한 요청 필드", example = "email")
    String field,
    @Schema(description = "검증 실패 사유", example = "이메일 형식이 올바르지 않습니다")
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

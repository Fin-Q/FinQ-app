package com.swyp.FinQ.global.success;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.ResponseEntity;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "API 성공 응답")
public record SuccessResponse<T>(
  @Schema(description = "요청 처리 상태", example = "SUCCESS", allowableValues = "SUCCESS")
  String status,
  @Schema(description = "요청 처리 결과 메시지", example = "요청 처리에 성공했습니다.")
  String message,
  @Schema(description = "API별 응답 데이터. 데이터가 없는 경우 생략될 수 있음")
  T data
) {

  public static <T> ResponseEntity<SuccessResponse<T>> of(SuccessCode code, T data) {
    return ResponseEntity
      .status(code.status())
      .body(new SuccessResponse<>("SUCCESS", code.message(), data));
  }

  public static ResponseEntity<Void> noContent() {
    return ResponseEntity.noContent().build();
  }
}

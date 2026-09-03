package com.swyp.FinQ.global.success;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.ResponseEntity;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SuccessResponse<T>(
  String status,
  String message,
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
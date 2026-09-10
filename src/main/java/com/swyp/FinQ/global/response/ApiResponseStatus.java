package com.swyp.FinQ.global.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "API 공통 처리 상태. SUCCESS=처리 성공, ERROR=처리 실패")
public enum ApiResponseStatus {
    SUCCESS,
    ERROR
}

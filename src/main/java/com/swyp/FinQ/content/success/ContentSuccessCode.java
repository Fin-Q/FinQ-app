package com.swyp.FinQ.content.success;

import com.swyp.FinQ.global.success.SuccessCode;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ContentSuccessCode implements SuccessCode {

    KNOWLEDGE_MAP_RETRIEVED(HttpStatus.OK, "지식맵 조회에 성공했습니다."),
    CATEGORY_DETAIL_RETRIEVED(HttpStatus.OK, "카테고리 상세 조회에 성공했습니다.");

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
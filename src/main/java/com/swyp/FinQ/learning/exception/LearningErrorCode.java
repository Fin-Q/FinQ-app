package com.swyp.FinQ.learning.exception;

import com.swyp.FinQ.global.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum LearningErrorCode implements ErrorCode {

    QUESTION_NOT_FOUND(HttpStatus.NOT_FOUND, "QUESTION_NOT_FOUND", "존재하지 않는 문제입니다."),
    QUIZ_NOT_FOUND(HttpStatus.NOT_FOUND, "QUIZ_NOT_FOUND", "존재하지 않는 심화퀴즈입니다."),
    INVALID_OPTION(HttpStatus.BAD_REQUEST, "QUIZ_INVALID_ANSWER", "유효하지 않은 보기입니다."),
    QUESTION_CONTENT_MISMATCH(HttpStatus.BAD_REQUEST, "QUESTION_CONTENT_MISMATCH", "해당 콘텐츠에 속하지 않는 문제입니다."),
    QUIZ_CATEGORY_MISMATCH(HttpStatus.BAD_REQUEST, "QUIZ_CATEGORY_MISMATCH", "해당 카테고리에 속하지 않는 심화퀴즈입니다.");

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
package com.swyp.FinQ.learning.success;

import com.swyp.FinQ.global.success.SuccessCode;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum LearningSuccessCode implements SuccessCode {

    CONTENT_ANSWER_GRADED(HttpStatus.OK, "요청에 성공했습니다."),
    QUIZ_LIST_RETRIEVED(HttpStatus.OK, "요청에 성공했습니다."),
    QUIZ_ANSWER_GRADED(HttpStatus.OK, "요청에 성공했습니다.");

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
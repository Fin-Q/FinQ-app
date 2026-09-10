package com.swyp.FinQ.user.success;

import com.swyp.FinQ.global.success.SuccessCode;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum UserSuccessCode implements SuccessCode {

    ONBOARDING_RETRIEVED(HttpStatus.OK, "온보딩 상태 조회에 성공했습니다."),
    INTERESTS_CREATED(HttpStatus.CREATED, "관심 주제 저장에 성공했습니다."),
    INTERESTS_UPDATED(HttpStatus.OK, "관심 주제 수정에 성공했습니다."),
    ONBOARDING_COMPLETED(HttpStatus.OK, "온보딩 완료 처리에 성공했습니다."),
    MY_PAGE_RETRIEVED(HttpStatus.OK, "마이페이지 조회에 성공했습니다."),
    NICKNAME_UPDATED(HttpStatus.OK, "닉네임 변경에 성공했습니다."),
    PROFILE_IMAGE_UPDATED(HttpStatus.OK, "프로필 이미지 변경에 성공했습니다."),
    KAKAO_ACCOUNT_LINKED(HttpStatus.OK, "Kakao 계정 연동에 성공했습니다."),
    APPLE_ACCOUNT_LINKED(HttpStatus.OK, "Apple 계정 연동에 성공했습니다."),
    USER_WITHDRAWN(HttpStatus.OK, "회원 탈퇴에 성공했습니다.");

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

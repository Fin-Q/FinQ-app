package com.swyp.FinQ.user.dto.res;

import com.swyp.FinQ.user.domain.ProfileImageCode;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "현재 사용자 프로필 이미지 조회 응답")
public record ProfileImageResponse(
        @Schema(description = "현재 사용자의 프로필 이미지 코드", example = "PROFILE_01")
        ProfileImageCode profileImageCode,
        @Schema(description = "현재 사용자 프로필 기본 이미지의 공개 HTTPS URL. 테두리·체크 및 편집 아이콘은 앱에서 표시",
                example = "https://assets.example.com/profile-images/profile_01.png", format = "uri")
        String profileImageUrl
) {
}

package com.swyp.FinQ.user.dto.res;

import com.swyp.FinQ.user.domain.ProfileImageCode;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "프로필 이미지 변경 응답")
public record ProfileImageUpdateResponse(
        @Schema(description = "변경된 프로필 이미지 코드", example = "PROFILE_02",
                allowableValues = {"PROFILE_01", "PROFILE_02", "PROFILE_03", "PROFILE_04"})
        ProfileImageCode profileImageCode,
        @Schema(description = "변경된 프로필 기본 이미지의 공개 HTTPS URL. 선택 테두리·체크 및 편집 아이콘은 앱에서 표시",
                example = "https://assets.example.com/profile-images/profile_02.png", format = "uri")
        String profileImageUrl
) {
}

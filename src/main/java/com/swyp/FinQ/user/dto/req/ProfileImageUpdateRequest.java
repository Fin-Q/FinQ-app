package com.swyp.FinQ.user.dto.req;

import com.swyp.FinQ.user.domain.ProfileImageCode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "프로필 이미지 변경 요청")
public record ProfileImageUpdateRequest(
        @Schema(description = "프로필 이미지 코드", example = "PROFILE_02")
        @NotNull(message = "프로필 이미지 코드는 필수입니다")
        ProfileImageCode profileImageCode
) {
}

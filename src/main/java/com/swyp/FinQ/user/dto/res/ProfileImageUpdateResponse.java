package com.swyp.FinQ.user.dto.res;

import com.swyp.FinQ.user.domain.ProfileImageCode;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "프로필 이미지 변경 응답")
public record ProfileImageUpdateResponse(
        @Schema(description = "변경된 프로필 이미지 코드", example = "PROFILE_02",
                allowableValues = {"PROFILE_01", "PROFILE_02", "PROFILE_03", "PROFILE_04"})
        ProfileImageCode profileImageCode
) {
}

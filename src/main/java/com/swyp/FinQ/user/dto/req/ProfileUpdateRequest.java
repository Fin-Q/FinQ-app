package com.swyp.FinQ.user.dto.req;

import com.swyp.FinQ.user.domain.ProfileImageCode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "프로필 수정 요청")
public record ProfileUpdateRequest(
        @Schema(description = "닉네임", example = "핀큐")
        @Size(max = 50, message = "닉네임은 50자 이하여야 합니다")
        @Pattern(regexp = ".*\\S.*", message = "닉네임은 공백일 수 없습니다")
        String nickname,

        @Schema(description = "고정 프로필 이미지 코드", example = "PROFILE_01")
        ProfileImageCode profileImageCode
) {
}

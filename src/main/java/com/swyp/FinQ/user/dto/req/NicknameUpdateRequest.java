package com.swyp.FinQ.user.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "닉네임 변경 요청")
public record NicknameUpdateRequest(
        @Schema(description = "변경할 닉네임", example = "핀큐마스터")
        @NotBlank(message = "닉네임은 공백일 수 없습니다")
        @Size(max = 50, message = "닉네임은 50자 이하여야 합니다")
        String nickname
) {
}

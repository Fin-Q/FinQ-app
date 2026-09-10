package com.swyp.FinQ.user.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;

@Schema(description = "닉네임 변경 응답")
public record NicknameUpdateResponse(
        @Schema(description = "변경된 닉네임", example = "핀큐마스터")
        String nickname,
        @Schema(description = "변경 일시, ISO 8601 KST 기준", example = "2026-09-10T10:30:00+09:00", format = "date-time")
        OffsetDateTime updatedAt
) {
}

package com.swyp.FinQ.reward.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "보상 상태 조회 응답")
public record RewardStatusResponse(
        @Schema(description = "누적 경험치", example = "120")
        int totalXp,
        @Schema(description = "현재 레벨", example = "2")
        int level,
        @Schema(description = "캐릭터 성장 단계", example = "2")
        int characterStage
) {
}
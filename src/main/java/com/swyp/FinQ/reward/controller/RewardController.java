package com.swyp.FinQ.reward.controller;

import com.swyp.FinQ.global.success.SuccessResponse;
import com.swyp.FinQ.reward.dto.res.RewardStatusResponse;
import com.swyp.FinQ.reward.service.XpQueryService;
import com.swyp.FinQ.reward.success.RewardSuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Reward", description = "보상 및 레벨 API")
@RestController
@RequestMapping("/rewards")
@RequiredArgsConstructor
public class RewardController {

    private final XpQueryService xpQueryService;

    @Operation(summary = "보상 상태 조회", description = "누적 XP, 레벨, 캐릭터 성장 단계를 조회합니다.")
    @GetMapping("/status")
    public ResponseEntity<SuccessResponse<RewardStatusResponse>> getRewardStatus(
            @AuthenticationPrincipal Jwt jwt
    ) {
        RewardStatusResponse response = xpQueryService.getRewardStatus(Long.valueOf(jwt.getSubject()));
        return SuccessResponse.of(RewardSuccessCode.REWARD_STATUS_RETRIEVED, response);
    }
}
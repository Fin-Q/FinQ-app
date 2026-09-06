package com.swyp.FinQ.user.dto.res;

import com.swyp.FinQ.user.domain.OnboardingStatus;

public record LoginResponse(
        String userId,
        String nickname,
        String accessToken,
        String refreshToken,
        String tokenType,
        long accessTokenExpiresIn,
        OnboardingStatus onboardingStatus
) {
}

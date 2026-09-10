package com.swyp.FinQ.user.repository;

import com.swyp.FinQ.user.domain.SocialProvider;

public record SocialAccountUnlinkTarget(
        SocialProvider provider,
        String providerUserId,
        String encryptedRefreshToken
) {
}

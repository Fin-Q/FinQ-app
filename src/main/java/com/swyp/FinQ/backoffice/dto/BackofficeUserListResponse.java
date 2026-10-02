package com.swyp.FinQ.backoffice.dto;

import java.time.LocalDateTime;
import java.util.List;

public record BackofficeUserListResponse(
        List<UserItem> users,
        long totalElements,
        int page,
        int size,
        int totalPages
) {
    public record UserItem(
            long id,
            String nickname,
            String email,
            String onboardingStatus,
            int totalXp,
            int currentStreak,
            LocalDateTime createdAt,
            LocalDateTime lastLoginAt
    ) {
    }
}

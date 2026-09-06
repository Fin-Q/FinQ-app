package com.swyp.FinQ.user.dto.res;

import com.swyp.FinQ.content.domain.CategoryCode;
import com.swyp.FinQ.reward.domain.Level;
import com.swyp.FinQ.user.domain.OnboardingStatus;
import com.swyp.FinQ.user.domain.ProfileImageCode;
import com.swyp.FinQ.user.domain.User;
import com.swyp.FinQ.user.domain.UserInterest;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "마이페이지 사용자 정보 응답")
public record MyPageResponse(
        @Schema(description = "사용자 ID", example = "1")
        String userId,
        @Schema(description = "이메일", example = "user@example.com")
        String email,
        @Schema(description = "닉네임", example = "핀큐")
        String nickname,
        @Schema(description = "프로필 이미지 코드", example = "PROFILE_01")
        ProfileImageCode profileImageCode,
        @Schema(description = "누적 XP", example = "80")
        int totalXp,
        @Schema(description = "현재 레벨", example = "LV2")
        Level level,
        @Schema(description = "현재 연속 스트릭 일수", example = "3")
        int currentStreakDays,
        @Schema(description = "알림 활성화 여부", example = "true")
        boolean notificationEnabled,
        @Schema(description = "온보딩 상태", example = "COMPLETED")
        OnboardingStatus onboardingStatus,
        @Schema(description = "관심 주제")
        List<InterestInfo> interests
) {

    public static MyPageResponse of(User user, int totalXp, List<UserInterest> interests) {
        return new MyPageResponse(
                String.valueOf(user.getId()),
                user.getEmail(),
                user.getNickname(),
                user.getProfileImageCode(),
                totalXp,
                Level.from(totalXp),
                user.getCurrentStreak(),
                user.isNotificationEnabled(),
                user.getOnboardingStatus(),
                interests.stream().map(InterestInfo::from).toList()
        );
    }

    @Schema(description = "관심 주제 정보")
    public record InterestInfo(
            @Schema(description = "카테고리 ID", example = "1")
            Long categoryId,
            @Schema(description = "카테고리 코드", example = "SAL")
            CategoryCode categoryCode,
            @Schema(description = "카테고리명", example = "월급관리·저축")
            String categoryName
    ) {

        private static InterestInfo from(UserInterest interest) {
            return new InterestInfo(
                    interest.getCategory().getId(),
                    interest.getCategory().getCategoryCode(),
                    interest.getCategory().getCategoryName()
            );
        }
    }
}

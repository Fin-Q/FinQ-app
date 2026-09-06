package com.swyp.FinQ.user.dto.res;

import com.swyp.FinQ.content.domain.CategoryCode;
import com.swyp.FinQ.user.domain.OnboardingStatus;
import com.swyp.FinQ.user.domain.User;
import com.swyp.FinQ.user.domain.UserInterest;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "온보딩 상태 응답")
public record OnboardingResponse(
        @Schema(description = "현재 온보딩 단계", example = "INTEREST_SECTION")
        OnboardingStatus onboardingStatus,
        @Schema(description = "선택한 관심 주제")
        List<InterestInfo> interests
) {

    public static OnboardingResponse of(User user, List<UserInterest> interests) {
        return new OnboardingResponse(
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

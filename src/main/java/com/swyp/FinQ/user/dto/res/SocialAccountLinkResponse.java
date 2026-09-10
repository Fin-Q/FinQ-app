package com.swyp.FinQ.user.dto.res;

import com.swyp.FinQ.user.domain.SocialAccount;
import com.swyp.FinQ.user.domain.SocialProvider;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@Schema(description = "소셜 계정 연동 결과")
public record SocialAccountLinkResponse(
        @Schema(description = "연동된 소셜 로그인 제공자", example = "APPLE", allowableValues = {"APPLE", "KAKAO"})
        SocialProvider socialProvider,
        @Schema(description = "최초 연동 일시, ISO 8601 KST 기준", example = "2026-09-10T10:30:00+09:00", format = "date-time")
        OffsetDateTime linkedAt
) {
    public static SocialAccountLinkResponse from(SocialAccount account) {
        return new SocialAccountLinkResponse(account.getProvider(),
                account.getLinkedAt().atZone(ZoneId.systemDefault())
                        .withZoneSameInstant(ZoneId.of("Asia/Seoul")).toOffsetDateTime());
    }
}

package com.swyp.FinQ.user.dto.res;

import com.swyp.FinQ.user.domain.SocialAccount;
import com.swyp.FinQ.user.domain.SocialProvider;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@Schema(description = "소셜 계정 연동 결과")
public record SocialAccountLinkResponse(
        SocialProvider socialProvider,
        @Schema(description = "최초 연동 일시, ISO 8601 KST 기준") OffsetDateTime linkedAt
) {
    public static SocialAccountLinkResponse from(SocialAccount account) {
        return new SocialAccountLinkResponse(account.getProvider(),
                account.getLinkedAt().atZone(ZoneId.systemDefault())
                        .withZoneSameInstant(ZoneId.of("Asia/Seoul")).toOffsetDateTime());
    }
}

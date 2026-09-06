package com.swyp.FinQ.user.dto.res;

import com.swyp.FinQ.user.domain.AgreementPolicy;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "현재 적용 중인 약관 목록 응답")
public record AgreementListResponse(
        @Schema(description = "약관 목록")
        List<AgreementInfo> agreements
) {

    public static AgreementListResponse from(List<AgreementPolicy> policies) {
        return new AgreementListResponse(policies.stream()
                .map(AgreementInfo::from)
                .toList());
    }

    @Schema(description = "약관 정보")
    public record AgreementInfo(
            @Schema(description = "약관 코드", example = "TERMS_OF_SERVICE")
            String agreementCode,
            @Schema(description = "약관 버전", example = "1.0")
            String version,
            @Schema(description = "필수 동의 여부", example = "true")
            boolean required
    ) {

        private static AgreementInfo from(AgreementPolicy policy) {
            return new AgreementInfo(policy.name(), policy.getVersion(), policy.isRequired());
        }
    }
}

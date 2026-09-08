package com.swyp.FinQ.user.service;

import com.swyp.FinQ.user.dto.req.AgreementRequest;

import java.util.List;

public record KakaoLoginCommand(
        String kakaoAccessToken,
        String nickname,
        List<AgreementRequest> agreements
) {
}

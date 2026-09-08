package com.swyp.FinQ.user.service;

import com.swyp.FinQ.user.dto.req.AgreementRequest;

import java.util.List;

public record AppleLoginCommand(
        String identityToken,
        String authorizationCode,
        String nonce,
        String nickname,
        List<AgreementRequest> agreements
) {
}

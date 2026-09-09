package com.swyp.FinQ.user.service;

import com.swyp.FinQ.user.domain.SocialProvider;
import com.swyp.FinQ.user.dto.req.KakaoAccountLinkRequest;
import com.swyp.FinQ.user.dto.res.SocialAccountLinkResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KakaoAccountLinkService {
    private final KakaoAccessTokenVerifier verifier;
    private final SocialAccountLinkService accountLinkService;

    public SocialAccountLinkResponse link(Long userId, KakaoAccountLinkRequest request) {
        KakaoUserIdentity identity = verifier.verify(request.kakaoAccessToken());
        return accountLinkService.link(userId, SocialProvider.KAKAO, identity.providerUserId());
    }
}

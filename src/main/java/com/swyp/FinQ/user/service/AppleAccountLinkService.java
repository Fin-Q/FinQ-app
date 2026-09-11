package com.swyp.FinQ.user.service;

import com.swyp.FinQ.user.domain.SocialProvider;
import com.swyp.FinQ.user.dto.req.AppleAccountLinkRequest;
import com.swyp.FinQ.user.dto.res.SocialAccountLinkResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AppleAccountLinkService {
    private final AppleIdentityTokenVerifier identityTokenVerifier;
    private final AppleAuthorizationCodeVerifier authorizationCodeVerifier;
    private final SocialAccountLinkService accountLinkService;
    private final OAuthTokenCipher tokenCipher;

    public SocialAccountLinkResponse link(Long userId, AppleAccountLinkRequest request) {
        AppleUserIdentity identity = identityTokenVerifier.verify(request.identityToken(), request.nonce());
        AppleAuthorizationResult authorization = authorizationCodeVerifier.verify(
                request.authorizationCode(),
                request.nonce(),
                identity
        );
        String encryptedRefreshToken = tokenCipher.encrypt(authorization.refreshToken());
        return accountLinkService.link(
                userId,
                SocialProvider.APPLE,
                identity.providerUserId(),
                encryptedRefreshToken
        );
    }
}

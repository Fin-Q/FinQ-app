package com.swyp.FinQ.user.service;

import com.nimbusds.jose.RemoteKeySourceException;
import com.swyp.FinQ.global.exception.BaseException;
import com.swyp.FinQ.user.config.AppleProperties;
import com.swyp.FinQ.user.exception.AuthErrorCode;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class AppleIdentityTokenClient implements AppleIdentityTokenVerifier {

    private final JwtDecoder jwtDecoder;
    private final AppleNonceHasher nonceHasher;
    private final String clientId;

    public AppleIdentityTokenClient(
            @Qualifier("appleIdentityTokenDecoder") JwtDecoder jwtDecoder,
            AppleNonceHasher nonceHasher,
            AppleProperties properties
    ) {
        this.jwtDecoder = jwtDecoder;
        this.nonceHasher = nonceHasher;
        this.clientId = properties.clientId();
    }

    @Override
    public AppleUserIdentity verify(String identityToken, String nonce) {
        validateConfiguration();
        if (!StringUtils.hasText(identityToken) || !StringUtils.hasText(nonce)) {
            throw BaseException.of(AuthErrorCode.INVALID_APPLE_IDENTITY_TOKEN);
        }

        try {
            Jwt jwt = jwtDecoder.decode(identityToken);
            String providerUserId = jwt.getSubject();
            String tokenNonce = jwt.getClaimAsString("nonce");
            String expectedNonce = nonceHasher.hash(nonce);

            if (!StringUtils.hasText(providerUserId) || !expectedNonce.equals(tokenNonce)) {
                throw BaseException.of(AuthErrorCode.INVALID_APPLE_IDENTITY_TOKEN);
            }
            return new AppleUserIdentity(providerUserId);
        } catch (JwtException | IllegalArgumentException exception) {
            if (hasCause(exception, RemoteKeySourceException.class)) {
                throw new BaseException(AuthErrorCode.APPLE_AUTH_SERVER_UNAVAILABLE, exception);
            }
            throw new BaseException(AuthErrorCode.INVALID_APPLE_IDENTITY_TOKEN, exception);
        }
    }

    private void validateConfiguration() {
        if (!StringUtils.hasText(clientId)) {
            throw new IllegalStateException("APPLE_CLIENT_ID 환경변수가 필요합니다.");
        }
    }

    private boolean hasCause(Throwable throwable, Class<? extends Throwable> causeType) {
        Throwable current = throwable;
        while (current != null) {
            if (causeType.isInstance(current)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}

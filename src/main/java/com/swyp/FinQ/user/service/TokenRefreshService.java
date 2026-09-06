package com.swyp.FinQ.user.service;

import com.swyp.FinQ.global.exception.BaseException;
import com.swyp.FinQ.global.security.token.IssuedTokenPair;
import com.swyp.FinQ.global.security.token.JwtTokenProvider;
import com.swyp.FinQ.global.security.token.RefreshTokenClaims;
import com.swyp.FinQ.user.domain.RefreshToken;
import com.swyp.FinQ.user.dto.req.TokenRefreshRequest;
import com.swyp.FinQ.user.dto.res.TokenRefreshResponse;
import com.swyp.FinQ.user.exception.AuthErrorCode;
import com.swyp.FinQ.user.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class TokenRefreshService {

    private static final ZoneId SERVICE_ZONE_ID = ZoneId.of("Asia/Seoul");

    private final JwtTokenProvider jwtTokenProvider;
    private final TokenHashEncoder tokenHashEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthTokenService authTokenService;
    private final Clock clock;

    @Transactional
    public TokenRefreshResponse refresh(TokenRefreshRequest request) {
        RefreshTokenClaims claims = parse(request.refreshToken());
        RefreshToken storedToken = refreshTokenRepository.findBySessionId(claims.sessionId())
                .orElseThrow(() -> BaseException.of(AuthErrorCode.INVALID_REFRESH_TOKEN));

        validate(storedToken, claims, request.refreshToken());
        refreshTokenRepository.delete(storedToken);

        IssuedTokenPair tokens = authTokenService.issue(storedToken.getUser());
        return new TokenRefreshResponse(
                tokens.accessToken(),
                tokens.refreshToken(),
                "Bearer",
                tokens.accessTokenExpiresIn()
        );
    }

    private RefreshTokenClaims parse(String refreshToken) {
        try {
            return jwtTokenProvider.parseRefreshToken(refreshToken);
        } catch (JwtException | IllegalArgumentException exception) {
            throw BaseException.of(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }
    }

    private void validate(RefreshToken storedToken, RefreshTokenClaims claims, String refreshToken) {
        LocalDateTime now = LocalDateTime.ofInstant(clock.instant(), SERVICE_ZONE_ID);
        boolean invalid = !storedToken.getUser().getId().equals(claims.userId())
                || !storedToken.getTokenHash().equals(tokenHashEncoder.encode(refreshToken))
                || !storedToken.getExpiresAt().isAfter(now);

        if (invalid) {
            throw BaseException.of(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }
    }
}

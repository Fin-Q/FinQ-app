package com.swyp.FinQ.user.service;

import com.swyp.FinQ.global.security.token.IssuedTokenPair;
import com.swyp.FinQ.global.security.token.JwtTokenProvider;
import com.swyp.FinQ.user.domain.RefreshToken;
import com.swyp.FinQ.user.domain.User;
import com.swyp.FinQ.user.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class AuthTokenService {

    private static final ZoneId SERVICE_ZONE_ID = ZoneId.of("Asia/Seoul");

    private final JwtTokenProvider jwtTokenProvider;
    private final TokenHashEncoder tokenHashEncoder;
    private final RefreshTokenRepository refreshTokenRepository;

    public IssuedTokenPair issue(User user) {
        IssuedTokenPair tokens = jwtTokenProvider.issue(user.getId());
        refreshTokenRepository.save(RefreshToken.builder()
                .user(user)
                .sessionId(tokens.sessionId())
                .tokenHash(tokenHashEncoder.encode(tokens.refreshToken()))
                .expiresAt(LocalDateTime.ofInstant(tokens.refreshTokenExpiresAt(), SERVICE_ZONE_ID))
                .build());
        return tokens;
    }
}

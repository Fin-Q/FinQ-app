package com.swyp.FinQ.global.security.token;

import com.swyp.FinQ.global.security.config.JwtProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Component
public class JwtTokenProvider {

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder tokenDecoder;
    private final JwtProperties properties;
    private final Clock clock;

    public JwtTokenProvider(
            JwtEncoder jwtEncoder,
            @Qualifier("tokenDecoder") JwtDecoder tokenDecoder,
            JwtProperties properties,
            Clock clock
    ) {
        this.jwtEncoder = jwtEncoder;
        this.tokenDecoder = tokenDecoder;
        this.properties = properties;
        this.clock = clock;
    }

    public IssuedTokenPair issue(Long userId) {
        Instant issuedAt = clock.instant();
        String sessionId = UUID.randomUUID().toString();
        Instant accessTokenExpiresAt = issuedAt.plus(properties.accessTokenExpiration());
        Instant refreshTokenExpiresAt = issuedAt.plus(properties.refreshTokenExpiration());

        String accessToken = encode(userId, sessionId, TokenType.ACCESS, issuedAt, accessTokenExpiresAt);
        String refreshToken = encode(userId, sessionId, TokenType.REFRESH, issuedAt, refreshTokenExpiresAt);

        return new IssuedTokenPair(
                accessToken,
                refreshToken,
                sessionId,
                properties.accessTokenExpiration().toSeconds(),
                properties.refreshTokenExpiration().toSeconds(),
                refreshTokenExpiresAt
        );
    }

    public RefreshTokenClaims parseRefreshToken(String token) {
        Jwt jwt = tokenDecoder.decode(token);
        if (!TokenType.REFRESH.name().equals(jwt.getClaimAsString(JwtClaimNames.TOKEN_TYPE))) {
            throw new IllegalArgumentException("Refresh Token이 아닙니다");
        }

        return new RefreshTokenClaims(
                Long.valueOf(jwt.getSubject()),
                jwt.getClaimAsString(JwtClaimNames.SESSION_ID),
                jwt.getExpiresAt()
        );
    }

    private String encode(
            Long userId,
            String sessionId,
            TokenType tokenType,
            Instant issuedAt,
            Instant expiresAt
    ) {
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(properties.issuer())
                .subject(String.valueOf(userId))
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .id(UUID.randomUUID().toString())
                .claim(JwtClaimNames.SESSION_ID, sessionId)
                .claim(JwtClaimNames.TOKEN_TYPE, tokenType.name())
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}

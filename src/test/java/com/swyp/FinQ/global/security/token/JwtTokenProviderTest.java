package com.swyp.FinQ.global.security.token;

import com.swyp.FinQ.global.security.config.JwtConfig;
import com.swyp.FinQ.global.security.config.JwtProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtEncoder;

import javax.crypto.SecretKey;
import java.time.Clock;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenProviderTest {

    private static final String SECRET = "ZmlucS10ZXN0LWp3dC1zZWNyZXQta2V5LTMyaXRlcyE=";

    private JwtTokenProvider tokenProvider;
    private JwtDecoder accessTokenDecoder;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties(
                SECRET,
                "finq",
                Duration.ofHours(1),
                Duration.ofDays(14)
        );
        JwtConfig config = new JwtConfig();
        SecretKey secretKey = config.jwtSecretKey(properties);
        JwtEncoder encoder = config.jwtEncoder(secretKey);
        JwtDecoder tokenDecoder = config.tokenDecoder(secretKey, properties);
        accessTokenDecoder = config.accessTokenDecoder(
                secretKey,
                properties,
                config.accessTokenTypeValidator()
        );
        tokenProvider = new JwtTokenProvider(
                encoder,
                tokenDecoder,
                properties,
                Clock.systemUTC()
        );
    }

    @Test
    void issuesAccessAndRefreshTokens() {
        IssuedTokenPair tokens = tokenProvider.issue(10L);

        Jwt accessToken = accessTokenDecoder.decode(tokens.accessToken());
        RefreshTokenClaims refreshToken = tokenProvider.parseRefreshToken(tokens.refreshToken());

        assertThat(accessToken.getSubject()).isEqualTo("10");
        assertThat(accessToken.getClaimAsString(JwtClaimNames.TOKEN_TYPE)).isEqualTo("ACCESS");
        assertThat(accessToken.getClaimAsString(JwtClaimNames.SESSION_ID)).isEqualTo(tokens.sessionId());
        assertThat(refreshToken.userId()).isEqualTo(10L);
        assertThat(refreshToken.sessionId()).isEqualTo(tokens.sessionId());
        assertThat(tokens.accessTokenExpiresIn()).isEqualTo(3600L);
        assertThat(tokens.refreshTokenExpiresIn()).isEqualTo(1209600L);
        assertThat(tokens.refreshTokenExpiresAt()).isAfter(accessToken.getExpiresAt());
    }

    @Test
    void rejectsRefreshTokenAsAccessToken() {
        IssuedTokenPair tokens = tokenProvider.issue(10L);

        assertThatThrownBy(() -> accessTokenDecoder.decode(tokens.refreshToken()))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void rejectsAccessTokenAsRefreshToken() {
        IssuedTokenPair tokens = tokenProvider.issue(10L);

        assertThatThrownBy(() -> tokenProvider.parseRefreshToken(tokens.accessToken()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Refresh Token이 아닙니다");
    }
}

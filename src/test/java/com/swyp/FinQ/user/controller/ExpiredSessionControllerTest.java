package com.swyp.FinQ.user.controller;

import com.swyp.FinQ.global.security.token.IssuedTokenPair;
import com.swyp.FinQ.global.security.token.JwtTokenProvider;
import com.swyp.FinQ.notification.domain.PushPlatform;
import com.swyp.FinQ.notification.domain.PushToken;
import com.swyp.FinQ.notification.repository.PushTokenRepository;
import com.swyp.FinQ.support.MySqlContainerSupport;
import com.swyp.FinQ.user.domain.ProfileImageCode;
import com.swyp.FinQ.user.domain.RefreshToken;
import com.swyp.FinQ.user.domain.User;
import com.swyp.FinQ.user.repository.RefreshTokenRepository;
import com.swyp.FinQ.user.repository.UserRepository;
import com.swyp.FinQ.user.service.TokenHashEncoder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ExpiredSessionControllerTest extends MySqlContainerSupport {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private RefreshTokenRepository refreshTokenRepository;
    @Autowired private PushTokenRepository pushTokenRepository;
    @Autowired private JwtTokenProvider jwtTokenProvider;
    @Autowired private TokenHashEncoder tokenHashEncoder;
    @Autowired private PlatformTransactionManager transactionManager;
    @MockitoBean private Clock clock;

    private final List<Long> userIds = new ArrayList<>();
    private Instant now;
    private TransactionTemplate transactions;

    @BeforeEach
    void setUp() {
        now = Instant.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS);
        given(clock.instant()).willReturn(now);
        transactions = new TransactionTemplate(transactionManager);
    }

    @AfterEach
    void cleanUp() {
        transactions.executeWithoutResult(status -> userIds.forEach(userId -> {
            pushTokenRepository.deleteAllByUser_Id(userId);
            refreshTokenRepository.deleteAllByUserId(userId);
            userRepository.deleteById(userId);
        }));
    }

    @ParameterizedTest
    @ValueSource(longs = {-1, 0})
    void commitsExpiredSessionDeletionAfterUnauthorizedResponse(long expirationOffset) throws Exception {
        Long userId = createUser();
        IssuedTokenPair expired = createSession(userId, now.plusSeconds(expirationOffset));
        IssuedTokenPair active = createSession(userId, now.plusSeconds(3600));

        assertInvalidRefresh(expired.refreshToken());

        transactions.executeWithoutResult(status -> {
            assertThat(refreshTokenRepository.findBySessionId(expired.sessionId())).isEmpty();
            assertThat(pushTokenRepository.findByDeviceId(expired.sessionId())).isEmpty();
            assertThat(refreshTokenRepository.findBySessionId(active.sessionId())).isPresent();
            assertThat(pushTokenRepository.findByDeviceId(active.sessionId())).isPresent();
        });
        assertInvalidRefresh(expired.refreshToken());
    }

    @Test
    void cleansUpExpiredJwtBeforeJwtParsing() throws Exception {
        Long userId = createUser();
        given(clock.instant()).willReturn(now.minusSeconds(15 * 24 * 3600));
        IssuedTokenPair expired = createSession(userId, now.minusSeconds(24 * 3600));
        given(clock.instant()).willReturn(now);

        assertInvalidRefresh(expired.refreshToken());

        transactions.executeWithoutResult(status -> {
            assertThat(refreshTokenRepository.findBySessionId(expired.sessionId())).isEmpty();
            assertThat(pushTokenRepository.findByDeviceId(expired.sessionId())).isEmpty();
        });
    }

    @Test
    void doesNotDeleteSessionsForTamperedOrUnknownTokens() throws Exception {
        Long userId = createUser();
        IssuedTokenPair stored = createSession(userId, now.minusSeconds(1));
        IssuedTokenPair unknown = jwtTokenProvider.issue(userId);

        assertInvalidRefresh(stored.refreshToken() + "tampered");
        assertInvalidRefresh(unknown.refreshToken());
        assertInvalidRefresh("not-a-jwt");

        transactions.executeWithoutResult(status -> {
            assertThat(refreshTokenRepository.findBySessionId(stored.sessionId())).isPresent();
            assertThat(pushTokenRepository.findByDeviceId(stored.sessionId())).isPresent();
        });
    }

    private Long createUser() {
        Long userId = transactions.execute(status -> userRepository.saveAndFlush(User.builder()
                .nickname("expired-session-test")
                .profileImageCode(ProfileImageCode.PROFILE_01)
                .build()).getId());
        userIds.add(userId);
        return userId;
    }

    private IssuedTokenPair createSession(Long userId, Instant expiresAt) {
        return transactions.execute(status -> {
            User user = userRepository.findById(userId).orElseThrow();
            IssuedTokenPair tokens = jwtTokenProvider.issue(userId);
            refreshTokenRepository.save(RefreshToken.builder()
                    .user(user)
                    .sessionId(tokens.sessionId())
                    .tokenHash(tokenHashEncoder.encode(tokens.refreshToken()))
                    .expiresAt(LocalDateTime.ofInstant(expiresAt, ZoneId.of("Asia/Seoul")))
                    .build());
            String fcmToken = UUID.randomUUID().toString();
            pushTokenRepository.save(PushToken.builder()
                    .user(user)
                    .sessionId(tokens.sessionId())
                    .deviceId(tokens.sessionId())
                    .fcmToken(fcmToken)
                    .fcmTokenHash(tokenHashEncoder.encode(fcmToken))
                    .platform(PushPlatform.IOS)
                    .build());
            return tokens;
        });
    }

    private void assertInvalidRefresh(String refreshToken) throws Exception {
        mockMvc.perform(post("/auth/token/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.errorCode").value("AUTH_INVALID_REFRESH_TOKEN"))
                .andExpect(jsonPath("$.message").value("유효하지 않은 Refresh Token입니다."))
                .andExpect(jsonPath("$.details").isEmpty())
                .andExpect(jsonPath("$.traceId").isString())
                .andExpect(jsonPath("$.data").doesNotExist());
    }
}

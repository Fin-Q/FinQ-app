package com.swyp.FinQ.user.controller;

import com.swyp.FinQ.support.MySqlContainerSupport;
import com.swyp.FinQ.user.domain.PasswordResetRequest;
import com.swyp.FinQ.user.domain.ProfileImageCode;
import com.swyp.FinQ.user.domain.RefreshToken;
import com.swyp.FinQ.user.domain.User;
import com.swyp.FinQ.user.repository.PasswordResetRequestRepository;
import com.swyp.FinQ.user.repository.RefreshTokenRepository;
import com.swyp.FinQ.user.repository.UserRepository;
import com.swyp.FinQ.user.service.TokenHashEncoder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PasswordResetControllerTest extends MySqlContainerSupport {
    @Autowired MockMvc mockMvc;
    @Autowired UserRepository users;
    @Autowired PasswordResetRequestRepository resets;
    @Autowired RefreshTokenRepository sessions;
    @Autowired PasswordEncoder passwords;
    @Autowired TokenHashEncoder hashes;

    private static final String TOKEN = "test-reset-token-with-high-entropy-fixture";
    private static final String NEW_PASSWORD = "NewPassword123!";

    @Test
    void resetsPasswordAndConsumesTokenWithoutAccessToken() throws Exception {
        PasswordResetRequest reset = fixture("valid");
        User user = reset.getUser();
        sessions.saveAndFlush(RefreshToken.builder().user(user).sessionId(UUID.randomUUID().toString())
                .tokenHash(hashes.encode("old-session")).expiresAt(now().plusDays(1)).build());

        mockMvc.perform(post("/auth/password-reset").contentType(MediaType.APPLICATION_JSON)
                        .content(body(TOKEN, NEW_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("비밀번호 재설정에 성공했습니다."))
                .andExpect(jsonPath("$.data").doesNotExist());

        users.flush();
        assertThat(passwords.matches(NEW_PASSWORD, user.getPassword())).isTrue();
        assertThat(passwords.matches("OldPassword123!", user.getPassword())).isFalse();
        assertThat(reset.getConsumedAt()).isNotNull();
        assertThat(sessions.findByTokenHash(hashes.encode("old-session"))).isEmpty();

        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + user.getEmail() + "\",\"password\":\"" + NEW_PASSWORD + "\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/auth/password-reset").contentType(MediaType.APPLICATION_JSON)
                        .content(body(TOKEN, "OtherPassword123!")))
                .andExpect(status().isBadRequest());
        assertThat(passwords.matches(NEW_PASSWORD, user.getPassword())).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"expired", "consumed", "unverified", "social", "missing"})
    void rejectsUnavailableTokensWithoutChangingPassword(String state) throws Exception {
        PasswordResetRequest reset = fixture(state);
        String previousPassword = reset.getUser().getPassword();
        mockMvc.perform(post("/auth/password-reset").contentType(MediaType.APPLICATION_JSON)
                        .content(body(state.equals("missing") ? "unknown-token" : TOKEN, NEW_PASSWORD)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("AUTH_INVALID_PASSWORD_RESET_REQUEST"));
        assertThat(reset.getUser().getPassword()).isEqualTo(previousPassword);
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"passwordResetToken\":null,\"newPassword\":\"NewPassword123!\"}",
            "{\"passwordResetToken\":\"token\",\"newPassword\":null}",
            "{\"passwordResetToken\":\"token\",\"newPassword\":\"short\"}",
            "{\"passwordResetToken\":\" \",\"newPassword\":\"NewPassword123!\"}",
            "{\"passwordResetToken\":\"token\",\"newPassword\":\"        \"}"})
    void validatesRequiredFields(String body) throws Exception {
        mockMvc.perform(post("/auth/password-reset").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("COMMON_VALIDATION_ERROR"));
    }

    @Test
    void rejectsPasswordOverMaximumLength() throws Exception {
        mockMvc.perform(post("/auth/password-reset").contentType(MediaType.APPLICATION_JSON)
                        .content(body(TOKEN, "a".repeat(73))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsPasswordExceedingBcryptByteLimit() throws Exception {
        mockMvc.perform(post("/auth/password-reset").contentType(MediaType.APPLICATION_JSON)
                        .content(body(TOKEN, "가".repeat(25))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("COMMON_VALIDATION_ERROR"));
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void concurrentRequestsConsumeTokenOnlyOnce() throws Exception {
        PasswordResetRequest reset = fixture("valid");
        try {
            try (var executor = Executors.newFixedThreadPool(2)) {
                CountDownLatch start = new CountDownLatch(1);
                java.util.concurrent.Callable<Integer> call = () -> {
                    if (!start.await(10, TimeUnit.SECONDS)) {
                        throw new IllegalStateException("Concurrent request start timed out");
                    }
                    return mockMvc.perform(post("/auth/password-reset")
                                    .contentType(MediaType.APPLICATION_JSON).content(body(TOKEN, NEW_PASSWORD)))
                            .andReturn().getResponse().getStatus();
                };
                var first = executor.submit(call);
                var second = executor.submit(call);
                start.countDown();
                assertThat(java.util.List.of(first.get(20, TimeUnit.SECONDS), second.get(20, TimeUnit.SECONDS)))
                        .containsExactlyInAnyOrder(200, 400);
            }
            assertThat(resets.findById(reset.getId()).orElseThrow().getConsumedAt()).isNotNull();
            assertThat(passwords.matches(NEW_PASSWORD,
                    users.findById(reset.getUser().getId()).orElseThrow().getPassword())).isTrue();
        } finally {
            resets.deleteById(reset.getId());
            users.deleteById(reset.getUser().getId());
        }
    }

    private PasswordResetRequest fixture(String state) {
        User user = users.saveAndFlush(User.builder().email(UUID.randomUUID() + "@example.com")
                .password(state.equals("social") ? null : passwords.encode("OldPassword123!"))
                .nickname("reset-user").profileImageCode(ProfileImageCode.PROFILE_01).build());
        return resets.saveAndFlush(PasswordResetRequest.builder().user(user)
                .verificationId(UUID.randomUUID().toString()).verificationCodeHash("unused-fixture-hash")
                .codeExpiresAt(now().minusMinutes(1)).resendAvailableAt(now().minusMinutes(2))
                .verifiedAt(state.equals("unverified") ? null : now().minusMinutes(3))
                .passwordResetTokenHash(hashes.encode(TOKEN))
                .tokenExpiresAt(state.equals("expired") ? now().minusSeconds(1) : now().plusMinutes(10))
                .consumedAt(state.equals("consumed") ? now().minusSeconds(1) : null).build());
    }

    private LocalDateTime now() {
        return LocalDateTime.now(ZoneId.of("Asia/Seoul"));
    }

    private String body(String token, String password) {
        return "{\"passwordResetToken\":\"" + token + "\",\"newPassword\":\"" + password + "\"}";
    }
}

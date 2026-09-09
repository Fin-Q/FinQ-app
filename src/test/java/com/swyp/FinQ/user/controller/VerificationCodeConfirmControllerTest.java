package com.swyp.FinQ.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swyp.FinQ.support.MySqlContainerSupport;
import com.swyp.FinQ.user.domain.PasswordResetRequest;
import com.swyp.FinQ.user.domain.ProfileImageCode;
import com.swyp.FinQ.user.domain.User;
import com.swyp.FinQ.user.repository.PasswordResetRequestRepository;
import com.swyp.FinQ.user.repository.RefreshTokenRepository;
import com.swyp.FinQ.user.repository.UserRepository;
import com.swyp.FinQ.user.service.PasswordResetMailSender;
import com.swyp.FinQ.user.service.TokenHashEncoder;
import com.swyp.FinQ.user.service.VerificationCodeEncoder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class VerificationCodeConfirmControllerTest extends MySqlContainerSupport {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired UserRepository users;
    @Autowired PasswordResetRequestRepository resets;
    @Autowired RefreshTokenRepository sessions;
    @Autowired PasswordEncoder passwords;
    @Autowired VerificationCodeEncoder codes;
    @Autowired TokenHashEncoder hashes;
    @Autowired PlatformTransactionManager transactions;
    @MockitoBean PasswordResetMailSender mail;
    private final List<Long> createdUsers = new ArrayList<>();
    private static final String PATH = "/auth/password-reset/verifications/confirm";

    @AfterEach
    void cleanup() {
        new TransactionTemplate(transactions).executeWithoutResult(status -> {
            for (Long id : createdUsers) {
                resets.deleteAllByUserId(id);
                sessions.deleteAllByUserId(id);
                users.deleteById(id);
            }
        });
    }

    @Test
    void sendsCodeConfirmsItAndResetsPassword() throws Exception {
        User user = user();
        String sent = mvc.perform(post("/auth/password-reset/verifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(java.util.Map.of("loginId", user.getEmail()))))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String id = json.readTree(sent).path("data").path("verificationId").asText();
        ArgumentCaptor<String> capturedCode = ArgumentCaptor.forClass(String.class);
        verify(mail).sendVerificationCode(eq(user.getEmail()), capturedCode.capture(), eq(Duration.ofMinutes(5)));

        String response = mvc.perform(post(PATH).contentType(MediaType.APPLICATION_JSON)
                        .content(body(id, capturedCode.getValue())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("인증번호 확인에 성공했습니다."))
                .andExpect(jsonPath("$.data.expiresIn").value(600))
                .andExpect(jsonPath("$.data.passwordResetToken").isString())
                .andReturn().getResponse().getContentAsString();
        String token = json.readTree(response).path("data").path("passwordResetToken").asText();
        PasswordResetRequest saved = resets.findByVerificationId(id).orElseThrow();
        assertThat(token).hasSize(43);
        assertThat(saved.getPasswordResetTokenHash()).isEqualTo(hashes.encode(token)).isNotEqualTo(token);
        assertThat(Duration.between(saved.getVerifiedAt(), saved.getTokenExpiresAt())).isEqualTo(Duration.ofMinutes(10));

        mvc.perform(post(PATH).contentType(MediaType.APPLICATION_JSON).content(body(id, capturedCode.getValue())))
                .andExpect(status().isBadRequest());
        assertThat(resets.findByVerificationId(id).orElseThrow().getPasswordResetTokenHash())
                .isEqualTo(hashes.encode(token));
        mvc.perform(post("/auth/password-reset").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(java.util.Map.of("passwordResetToken", token,
                                "newPassword", "NewPassword123!"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data").doesNotExist());
        assertThat(passwords.matches("NewPassword123!", users.findById(user.getId()).orElseThrow().getPassword())).isTrue();
    }

    @Test
    void persistsEachFailedAttemptAndLocksAfterFive() throws Exception {
        PasswordResetRequest request = fixture("valid");
        for (int attempt = 1; attempt <= 5; attempt++) {
            mvc.perform(post(PATH).contentType(MediaType.APPLICATION_JSON).content(body(request.getVerificationId(), "999999")))
                    .andExpect(status().isBadRequest());
            assertThat(resets.findById(request.getId()).orElseThrow().getFailedAttemptCount()).isEqualTo(attempt);
        }
        mvc.perform(post(PATH).contentType(MediaType.APPLICATION_JSON).content(body(request.getVerificationId(), "123456")))
                .andExpect(status().isBadRequest());
        PasswordResetRequest locked = resets.findById(request.getId()).orElseThrow();
        assertThat(locked.getFailedAttemptCount()).isEqualTo(5);
        assertThat(locked.getPasswordResetTokenHash()).isNull();
    }

    @Test
    void acceptsCorrectCodeAfterFourFailures() throws Exception {
        PasswordResetRequest request = fixture("valid");
        for (int i = 0; i < 4; i++) {
            mvc.perform(post(PATH).contentType(MediaType.APPLICATION_JSON).content(body(request.getVerificationId(), "999999")))
                    .andExpect(status().isBadRequest());
        }
        mvc.perform(post(PATH).contentType(MediaType.APPLICATION_JSON).content(body(request.getVerificationId(), "123456")))
                .andExpect(status().isOk());
    }

    @Test
    void resendReplacesLockedRequestAndAllowsNewCode() throws Exception {
        PasswordResetRequest request = fixture("valid");
        for (int i = 0; i < 5; i++) {
            mvc.perform(post(PATH).contentType(MediaType.APPLICATION_JSON)
                            .content(body(request.getVerificationId(), "999999")))
                    .andExpect(status().isBadRequest());
        }
        String email = request.getUser().getEmail();
        String response = mvc.perform(post("/auth/password-reset/verifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(java.util.Map.of("loginId", email))))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String newId = json.readTree(response).path("data").path("verificationId").asText();
        ArgumentCaptor<String> capturedCode = ArgumentCaptor.forClass(String.class);
        verify(mail).sendVerificationCode(eq(email), capturedCode.capture(), eq(Duration.ofMinutes(5)));
        assertThat(resets.findByVerificationId(request.getVerificationId())).isEmpty();
        mvc.perform(post(PATH).contentType(MediaType.APPLICATION_JSON).content(body(newId, capturedCode.getValue())))
                .andExpect(status().isOk());
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"verificationId\":null,\"verificationCode\":\"123456\"}",
            "{\"verificationId\":\"id\",\"verificationCode\":null}"})
    void rejectsMissingFields(String request) throws Exception {
        mvc.perform(post(PATH).contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("COMMON_VALIDATION_ERROR"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"expired", "verified", "consumed", "unknown"})
    void rejectsUnavailableRequests(String state) throws Exception {
        PasswordResetRequest request = fixture(state);
        mvc.perform(post(PATH).contentType(MediaType.APPLICATION_JSON)
                        .content(body(state.equals("unknown") ? "unknown" : request.getVerificationId(), "123456")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("AUTH_INVALID_PASSWORD_RESET_REQUEST"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "12345", "1234567", "abcdef", "１２３４５６"})
    void rejectsMalformedCode(String code) throws Exception {
        mvc.perform(post(PATH).contentType(MediaType.APPLICATION_JSON).content(body("id", code)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("COMMON_VALIDATION_ERROR"));
    }

    @Test
    void concurrentConfirmationsIssueOnlyOneToken() throws Exception {
        PasswordResetRequest request = fixture("valid");
        try (var executor = Executors.newFixedThreadPool(2)) {
            CountDownLatch start = new CountDownLatch(1);
            java.util.concurrent.Callable<Integer> call = () -> {
                if (!start.await(10, TimeUnit.SECONDS)) throw new IllegalStateException("Start timed out");
                return mvc.perform(post(PATH).contentType(MediaType.APPLICATION_JSON)
                                .content(body(request.getVerificationId(), "123456")))
                        .andReturn().getResponse().getStatus();
            };
            var first = executor.submit(call);
            var second = executor.submit(call);
            start.countDown();
            assertThat(List.of(first.get(20, TimeUnit.SECONDS), second.get(20, TimeUnit.SECONDS)))
                    .containsExactlyInAnyOrder(200, 400);
        }
    }

    private User user() {
        User user = users.saveAndFlush(User.builder().email(UUID.randomUUID() + "@example.com")
                .password(passwords.encode("OldPassword123!")).nickname("verification-user")
                .profileImageCode(ProfileImageCode.PROFILE_01).build());
        createdUsers.add(user.getId());
        return user;
    }

    private PasswordResetRequest fixture(String state) {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        return resets.saveAndFlush(PasswordResetRequest.builder().user(user())
                .verificationId(UUID.randomUUID().toString()).verificationCodeHash(codes.encode("123456"))
                .codeExpiresAt(state.equals("expired") ? now.minusSeconds(1) : now.plusMinutes(5))
                .resendAvailableAt(now.minusMinutes(1))
                .verifiedAt(state.equals("verified") ? now : null)
                .consumedAt(state.equals("consumed") ? now : null).build());
    }

    private String body(String id, String code) throws Exception {
        return json.writeValueAsString(java.util.Map.of("verificationId", id, "verificationCode", code));
    }
}

package com.swyp.FinQ.user.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swyp.FinQ.support.MySqlContainerSupport;
import com.swyp.FinQ.notification.repository.PushTokenRepository;
import com.swyp.FinQ.user.domain.OnboardingStatus;
import com.swyp.FinQ.user.domain.ProfileImageCode;
import com.swyp.FinQ.user.domain.RefreshToken;
import com.swyp.FinQ.user.domain.PasswordResetRequest;
import com.swyp.FinQ.user.domain.SocialProvider;
import com.swyp.FinQ.user.domain.User;
import com.swyp.FinQ.user.domain.UserAgreement;
import com.swyp.FinQ.user.repository.RefreshTokenRepository;
import com.swyp.FinQ.user.repository.PasswordResetRequestRepository;
import com.swyp.FinQ.user.repository.SocialAccountRepository;
import com.swyp.FinQ.user.repository.UserAgreementRepository;
import com.swyp.FinQ.user.repository.UserRepository;
import com.swyp.FinQ.user.service.TokenHashEncoder;
import com.swyp.FinQ.user.service.AppleAuthorizationCodeVerifier;
import com.swyp.FinQ.user.service.AppleIdentityTokenVerifier;
import com.swyp.FinQ.user.service.AppleUserIdentity;
import com.swyp.FinQ.user.service.KakaoAccessTokenVerifier;
import com.swyp.FinQ.user.service.KakaoUserIdentity;
import com.swyp.FinQ.user.service.PasswordResetMailSender;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerTest extends MySqlContainerSupport {

    private static final String APPLE_RAW_NONCE = "0123456789abcdef0123456789abcdef";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserAgreementRepository userAgreementRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PushTokenRepository pushTokenRepository;

    @Autowired
    private SocialAccountRepository socialAccountRepository;

    @Autowired
    private PasswordResetRequestRepository passwordResetRequestRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TokenHashEncoder tokenHashEncoder;

    @MockitoBean
    private PasswordResetMailSender passwordResetMailSender;

    @MockitoBean
    private KakaoAccessTokenVerifier kakaoAccessTokenVerifier;

    @MockitoBean
    private AppleIdentityTokenVerifier appleIdentityTokenVerifier;

    @MockitoBean
    private AppleAuthorizationCodeVerifier appleAuthorizationCodeVerifier;

    @Test
    void getsCurrentAgreementsWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/auth/agreements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("약관 목록 조회에 성공했습니다."))
                .andExpect(jsonPath("$.data.agreements.length()").value(2))
                .andExpect(jsonPath("$.data.agreements[0].agreementCode").value("TERMS_OF_SERVICE"))
                .andExpect(jsonPath("$.data.agreements[0].version").value("1.0"))
                .andExpect(jsonPath("$.data.agreements[0].required").value(true))
                .andExpect(jsonPath("$.data.agreements[1].agreementCode").value("PRIVACY_POLICY"))
                .andExpect(jsonPath("$.data.agreements[1].version").value("1.0"))
                .andExpect(jsonPath("$.data.agreements[1].required").value(true));
    }

    @Test
    void signsUpWithAgreementsAndTokens() throws Exception {
        String responseBody = mockMvc.perform(post("/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validSignUpRequest()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("회원가입에 성공했습니다."))
                .andExpect(jsonPath("$.data.userId").isString())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.accessTokenExpiresIn").value(3600))
                .andExpect(jsonPath("$.data.onboardingStatus").value("INTEREST_SELECTION"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode data = objectMapper.readTree(responseBody).get("data");
        User user = userRepository.findByEmail("user@example.com").orElseThrow();
        List<UserAgreement> agreements = userAgreementRepository.findAllByUserId(user.getId());
        RefreshToken refreshToken = refreshTokenRepository.findAll().getFirst();

        assertThat(data.get("userId").asText()).isEqualTo(String.valueOf(user.getId()));
        assertThat(passwordEncoder.matches("Password123!", user.getPassword())).isTrue();
        assertThat(EnumSet.allOf(ProfileImageCode.class)).contains(user.getProfileImageCode());
        assertThat(user.getLastLoginAt()).isEqualTo(user.getCreatedAt());
        assertThat(agreements).hasSize(2);
        assertThat(agreements).allMatch(UserAgreement::isAgreed);
        assertThat(agreements).allMatch(agreement -> agreement.getAgreedAt().equals(user.getCreatedAt()));
        assertThat(refreshToken.getUser().getId()).isEqualTo(user.getId());
        assertThat(refreshToken.getTokenHash())
                .isEqualTo(tokenHashEncoder.encode(data.get("refreshToken").asText()));
        assertThat(refreshToken.getTokenHash()).doesNotContain(data.get("refreshToken").asText());
    }

    @Test
    void rejectsExistingEmail() throws Exception {
        userRepository.save(User.builder()
                .email("user@example.com")
                .password("encoded-password")
                .nickname("Existing")
                .profileImageCode(ProfileImageCode.PROFILE_01)
                .onboardingStatus(OnboardingStatus.INTEREST_SELECTION)
                .build());

        mockMvc.perform(post("/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validSignUpRequest()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.errorCode").value("AUTH_EMAIL_ALREADY_EXISTS"));
    }

    @Test
    void rejectsMissingRequiredAgreement() throws Exception {
        String request = """
                {
                  "email": "user@example.com",
                  "password": "Password123!",
                  "nickname": "Minter",
                  "agreements": [
                    {
                      "agreementCode": "TERMS_OF_SERVICE",
                      "version": "1.0",
                      "agreed": true
                    }
                  ]
                }
                """;

        mockMvc.perform(post("/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("AUTH_REQUIRED_AGREEMENT_MISSING"));
    }

    @Test
    void rejectsRequiredAgreementNotAccepted() throws Exception {
        String request = """
                {
                  "email": "user@example.com",
                  "password": "Password123!",
                  "nickname": "Minter",
                  "agreements": [
                    {
                      "agreementCode": "TERMS_OF_SERVICE",
                      "version": "1.0",
                      "agreed": true
                    },
                    {
                      "agreementCode": "PRIVACY_POLICY",
                      "version": "1.0",
                      "agreed": false
                    }
                  ]
                }
                """;

        mockMvc.perform(post("/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("AUTH_REQUIRED_AGREEMENT_NOT_ACCEPTED"));
    }

    @Test
    void logsInWithEmailAndPassword() throws Exception {
        User user = saveUser("user@example.com", "Password123!");
        LocalDateTime previousLoginAt = user.getLastLoginAt();

        String responseBody = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validLoginRequest()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("로그인에 성공했습니다."))
                .andExpect(jsonPath("$.data.userId").value(String.valueOf(user.getId())))
                .andExpect(jsonPath("$.data.nickname").value("Minter"))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.accessTokenExpiresIn").value(3600))
                .andExpect(jsonPath("$.data.onboardingStatus").value("INTEREST_SELECTION"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode data = objectMapper.readTree(responseBody).get("data");
        User loggedInUser = userRepository.findById(user.getId()).orElseThrow();
        RefreshToken refreshToken = refreshTokenRepository.findAll().getFirst();

        assertThat(loggedInUser.getLastLoginAt()).isAfter(previousLoginAt);
        assertThat(refreshToken.getUser().getId()).isEqualTo(user.getId());
        assertThat(refreshToken.getTokenHash())
                .isEqualTo(tokenHashEncoder.encode(data.get("refreshToken").asText()));
    }

    @Test
    void rejectsLoginWithUnknownEmail() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validLoginRequest()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.errorCode").value("AUTH_INVALID_CREDENTIALS"));
    }

    @Test
    void rejectsLoginWithWrongPassword() throws Exception {
        saveUser("user@example.com", "Password123!");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "user@example.com",
                                  "password": "WrongPassword!"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("AUTH_INVALID_CREDENTIALS"));
    }

    @Test
    void registersNewUserAndLogsInWithKakao() throws Exception {
        given(kakaoAccessTokenVerifier.verify("valid-kakao-token"))
                .willReturn(new KakaoUserIdentity("123456789"));

        String responseBody = mockMvc.perform(post("/auth/social/kakao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validNewKakaoLoginRequest()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("Kakao 로그인에 성공했습니다."))
                .andExpect(jsonPath("$.data.userId").isString())
                .andExpect(jsonPath("$.data.nickname").value("Minter"))
                .andExpect(jsonPath("$.data.isNewUser").value(true))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.accessTokenExpiresIn").value(3600))
                .andExpect(jsonPath("$.data.onboardingStatus").value("INTEREST_SELECTION"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode data = objectMapper.readTree(responseBody).path("data");
        Long userId = Long.valueOf(data.path("userId").asText());

        assertThat(socialAccountRepository
                .findByProviderAndProviderUserId(SocialProvider.KAKAO, "123456789"))
                .get()
                .extracting(account -> account.getUser().getId())
                .isEqualTo(userId);
        assertThat(userAgreementRepository.findAllByUserId(userId)).hasSize(2);
        assertThat(refreshTokenRepository.findAll()).hasSize(1);
    }

    @Test
    void logsInExistingKakaoUserWithoutNicknameAndAgreements() throws Exception {
        given(kakaoAccessTokenVerifier.verify("valid-kakao-token"))
                .willReturn(new KakaoUserIdentity("123456789"));
        mockMvc.perform(post("/auth/social/kakao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validNewKakaoLoginRequest()))
                .andExpect(status().isOk());

        mockMvc.perform(post("/auth/social/kakao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "kakaoAccessToken": "valid-kakao-token"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nickname").value("Minter"))
                .andExpect(jsonPath("$.data.isNewUser").value(false))
                .andExpect(jsonPath("$.message").value("Kakao 로그인에 성공했습니다."))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty());

        assertThat(userRepository.count()).isOne();
        assertThat(socialAccountRepository.count()).isOne();
        assertThat(userAgreementRepository.count()).isEqualTo(2);
        assertThat(refreshTokenRepository.count()).isEqualTo(2);
    }

    @Test
    void rejectsNewKakaoUserWithoutNickname() throws Exception {
        given(kakaoAccessTokenVerifier.verify("valid-kakao-token"))
                .willReturn(new KakaoUserIdentity("123456789"));

        mockMvc.perform(post("/auth/social/kakao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "kakaoAccessToken": "valid-kakao-token",
                                  "agreements": [
                                    {"agreementCode": "TERMS_OF_SERVICE", "version": "1.0", "agreed": true},
                                    {"agreementCode": "PRIVACY_POLICY", "version": "1.0", "agreed": true}
                                  ]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("AUTH_INVALID_KAKAO_SIGN_UP_INFO"));
    }

    @Test
    void rejectsKakaoLoginWithoutAccessToken() throws Exception {
        mockMvc.perform(post("/auth/social/kakao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verify(kakaoAccessTokenVerifier, never()).verify(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void documentsKakaoLoginInOpenApi() throws Exception {
        mockMvc.perform(get("/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$['paths']['/auth/social/kakao']['post']['summary']")
                        .value("Kakao 소셜 로그인"));
    }

    @Test
    void 신규_Apple_회원가입과_로그인에_성공한다() throws Exception {
        given(appleIdentityTokenVerifier.verify("valid-apple-identity-token", APPLE_RAW_NONCE))
                .willReturn(new AppleUserIdentity("apple-user-id"));

        String responseBody = mockMvc.perform(post("/auth/social/apple")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validNewAppleLoginRequest()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("Apple 로그인에 성공했습니다."))
                .andExpect(jsonPath("$.data.userId").isString())
                .andExpect(jsonPath("$.data.nickname").value("Minter"))
                .andExpect(jsonPath("$.data.isNewUser").value(true))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.accessTokenExpiresIn").value(3600))
                .andExpect(jsonPath("$.data.onboardingStatus").value("INTEREST_SELECTION"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long userId = Long.valueOf(objectMapper.readTree(responseBody)
                .path("data")
                .path("userId")
                .asText());
        assertThat(socialAccountRepository
                .findByProviderAndProviderUserId(SocialProvider.APPLE, "apple-user-id"))
                .get()
                .extracting(account -> account.getUser().getId())
                .isEqualTo(userId);
        assertThat(userAgreementRepository.findAllByUserId(userId)).hasSize(2);
        assertThat(refreshTokenRepository.findAll()).hasSize(1);
        verify(appleAuthorizationCodeVerifier).verify(
                "valid-apple-authorization-code",
                APPLE_RAW_NONCE,
                new AppleUserIdentity("apple-user-id")
        );
    }

    @Test
    void 기존_Apple_회원은_닉네임과_약관_없이_로그인한다() throws Exception {
        given(appleIdentityTokenVerifier.verify("valid-apple-identity-token", APPLE_RAW_NONCE))
                .willReturn(new AppleUserIdentity("apple-user-id"));
        mockMvc.perform(post("/auth/social/apple")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validNewAppleLoginRequest()))
                .andExpect(status().isOk());

        mockMvc.perform(post("/auth/social/apple")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "identityToken": "valid-apple-identity-token",
                                  "authorizationCode": "valid-apple-authorization-code",
                                  "nonce": "%s"
                                }
                                """.formatted(APPLE_RAW_NONCE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Apple 로그인에 성공했습니다."))
                .andExpect(jsonPath("$.data.nickname").value("Minter"))
                .andExpect(jsonPath("$.data.isNewUser").value(false))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty());

        assertThat(userRepository.count()).isOne();
        assertThat(socialAccountRepository.count()).isOne();
        assertThat(userAgreementRepository.count()).isEqualTo(2);
        assertThat(refreshTokenRepository.count()).isEqualTo(2);
    }

    @Test
    void Apple_필수_인증값이_누락되면_요청을_거부한다() throws Exception {
        mockMvc.perform(post("/auth/social/apple")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "identityToken": "valid-apple-identity-token",
                                  "authorizationCode": "valid-apple-authorization-code"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verify(appleIdentityTokenVerifier, never()).verify(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any()
        );
    }

    @Test
    void Apple_raw_nonce가_32자가_아니면_요청을_거부한다() throws Exception {
        for (String invalidNonce : List.of("a".repeat(31), "a".repeat(33))) {
            mockMvc.perform(post("/auth/social/apple")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "identityToken": "valid-apple-identity-token",
                                      "authorizationCode": "valid-apple-authorization-code",
                                      "nonce": "%s"
                                    }
                                    """.formatted(invalidNonce)))
                    .andExpect(status().isBadRequest());
        }

        verify(appleIdentityTokenVerifier, never()).verify(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any()
        );
    }

    @Test
    void Apple_로그인_API를_OpenAPI에_문서화한다() throws Exception {
        mockMvc.perform(get("/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$['paths']['/auth/social/apple']['post']['summary']")
                        .value("Apple 소셜 로그인"))
                .andExpect(jsonPath("$['components']['schemas']['AppleLoginRequest']['required'].length()")
                        .value(3));
    }

    @Test
    void rotatesRefreshToken() throws Exception {
        saveUser("user@example.com", "Password123!");
        String loginResponse = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validLoginRequest()))
                .andReturn()
                .getResponse()
                .getContentAsString();
        String oldRefreshToken = objectMapper.readTree(loginResponse)
                .path("data")
                .path("refreshToken")
                .asText();

        String refreshResponse = mockMvc.perform(post("/auth/token/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RefreshTokenBody(oldRefreshToken))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("토큰 재발급에 성공했습니다."))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.accessTokenExpiresIn").value(3600))
                .andExpect(jsonPath("$.data.refreshTokenExpiresIn").value(1209600))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode data = objectMapper.readTree(refreshResponse).path("data");
        String newRefreshToken = data.path("refreshToken").asText();

        assertThat(newRefreshToken).isNotEqualTo(oldRefreshToken);
        assertThat(refreshTokenRepository.findByTokenHash(tokenHashEncoder.encode(oldRefreshToken))).isEmpty();
        assertThat(refreshTokenRepository.findByTokenHash(tokenHashEncoder.encode(newRefreshToken))).isPresent();
    }

    @Test
    void rejectsReusedRefreshToken() throws Exception {
        saveUser("user@example.com", "Password123!");
        String loginResponse = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validLoginRequest()))
                .andReturn()
                .getResponse()
                .getContentAsString();
        String refreshToken = objectMapper.readTree(loginResponse)
                .path("data")
                .path("refreshToken")
                .asText();
        String request = objectMapper.writeValueAsString(new RefreshTokenBody(refreshToken));

        mockMvc.perform(post("/auth/token/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk());

        mockMvc.perform(post("/auth/token/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("AUTH_INVALID_REFRESH_TOKEN"));
    }

    @Test
    void logsOutCurrentSession() throws Exception {
        saveUser("user@example.com", "Password123!");
        JsonNode loginData = login();
        String accessToken = loginData.path("accessToken").asText();
        String refreshToken = loginData.path("refreshToken").asText();
        registerPushToken(accessToken, "device-1", "fcm-token-1");

        mockMvc.perform(post("/auth/logout")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("로그아웃에 성공했습니다."))
                .andExpect(jsonPath("$.data").doesNotExist());

        assertThat(refreshTokenRepository.findByTokenHash(tokenHashEncoder.encode(refreshToken))).isEmpty();
        assertThat(pushTokenRepository.findByDeviceId("device-1")).isEmpty();

        mockMvc.perform(post("/auth/token/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RefreshTokenBody(refreshToken))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("AUTH_INVALID_REFRESH_TOKEN"));
    }

    @Test
    void preservesOtherSessionOnLogout() throws Exception {
        saveUser("user@example.com", "Password123!");
        JsonNode firstSession = login();
        JsonNode secondSession = login();
        registerPushToken(firstSession.path("accessToken").asText(), "device-1", "fcm-token-1");
        registerPushToken(secondSession.path("accessToken").asText(), "device-2", "fcm-token-2");

        mockMvc.perform(post("/auth/logout")
                        .header("Authorization", "Bearer " + firstSession.path("accessToken").asText()))
                .andExpect(status().isOk());

        assertThat(refreshTokenRepository.findByTokenHash(
                tokenHashEncoder.encode(firstSession.path("refreshToken").asText())
        )).isEmpty();
        assertThat(refreshTokenRepository.findByTokenHash(
                tokenHashEncoder.encode(secondSession.path("refreshToken").asText())
        )).isPresent();
        assertThat(pushTokenRepository.findByDeviceId("device-1")).isEmpty();
        assertThat(pushTokenRepository.findByDeviceId("device-2")).isPresent();
    }

    @Test
    void rejectsLogoutWithoutAccessToken() throws Exception {
        mockMvc.perform(post("/auth/logout"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("AUTH_UNAUTHORIZED"));
    }

    @Test
    void requestsPasswordResetCodeWithoutAuthentication() throws Exception {
        User user = saveUser("user@example.com", "Password123!");

        String responseBody = mockMvc.perform(post("/auth/password-reset/verifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "loginId": "user@example.com"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("인증번호를 전송했습니다."))
                .andExpect(jsonPath("$.data.verificationId").isString())
                .andExpect(jsonPath("$.data.expiresIn").value(300))
                .andExpect(jsonPath("$.data.resendAvailableIn").value(60))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String verificationId = objectMapper.readTree(responseBody)
                .path("data")
                .path("verificationId")
                .asText();
        PasswordResetRequest resetRequest = passwordResetRequestRepository
                .findByVerificationId(verificationId)
                .orElseThrow();

        assertThat(resetRequest.getUser().getId()).isEqualTo(user.getId());
        assertThat(resetRequest.getVerificationCodeHash()).isNotBlank();
        verify(passwordResetMailSender).sendVerificationCode(
                eq("user@example.com"),
                org.mockito.ArgumentMatchers.matches("\\d{6}"),
                eq(java.time.Duration.ofMinutes(5))
        );
    }

    @Test
    void returnsSamePasswordResetResponseForUnknownEmail() throws Exception {
        mockMvc.perform(post("/auth/password-reset/verifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "loginId": "unknown@example.com"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("인증번호를 전송했습니다."))
                .andExpect(jsonPath("$.data.verificationId").isString())
                .andExpect(jsonPath("$.data.expiresIn").value(300))
                .andExpect(jsonPath("$.data.resendAvailableIn").value(60));

        assertThat(passwordResetRequestRepository.count()).isZero();
        verify(passwordResetMailSender, never()).sendVerificationCode(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any()
        );
    }

    @Test
    void rejectsPasswordResetResendBeforeCooldown() throws Exception {
        User user = saveUser("user@example.com", "Password123!");
        PasswordResetRequest request = passwordResetRequestRepository.saveAndFlush(PasswordResetRequest.builder()
                .user(user)
                .verificationId("verification-id")
                .verificationCodeHash("encoded-code")
                .codeExpiresAt(LocalDateTime.now().plusMinutes(5))
                .resendAvailableAt(LocalDateTime.now().plusMinutes(1))
                .build());

        mockMvc.perform(post("/auth/password-reset/verifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "loginId": "user@example.com"
                                }
                                """))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.errorCode").value("AUTH_PASSWORD_RESET_RESEND_TOO_EARLY"));

        assertThat(request.getVerificationCodeHash()).isEqualTo("encoded-code");
    }

    @Test
    void resendsPasswordResetCodeAfterCooldown() throws Exception {
        User user = saveUser("user@example.com", "Password123!");
        passwordResetRequestRepository.saveAndFlush(PasswordResetRequest.builder()
                .user(user)
                .verificationId("verification-id")
                .verificationCodeHash("old-code-hash")
                .codeExpiresAt(LocalDateTime.now().minusMinutes(1))
                .resendAvailableAt(LocalDateTime.now().minusSeconds(1))
                .failedAttemptCount(2)
                .build());

        String responseBody = mockMvc.perform(post("/auth/password-reset/verifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "loginId": "user@example.com"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.verificationId").isString())
                .andExpect(jsonPath("$.data.expiresIn").value(300))
                .andExpect(jsonPath("$.data.resendAvailableIn").value(60))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String renewedVerificationId = objectMapper.readTree(responseBody)
                .path("data")
                .path("verificationId")
                .asText();
        assertThat(renewedVerificationId).isNotEqualTo("verification-id");
        assertThat(passwordResetRequestRepository.findByVerificationId("verification-id")).isEmpty();
        PasswordResetRequest renewedRequest = passwordResetRequestRepository
                .findByVerificationId(renewedVerificationId)
                .orElseThrow();
        assertThat(renewedRequest.getFailedAttemptCount()).isZero();
        assertThat(renewedRequest.getCodeExpiresAt()).isAfter(LocalDateTime.now().plusMinutes(4));
        verify(passwordResetMailSender).sendVerificationCode(
                eq("user@example.com"),
                org.mockito.ArgumentMatchers.matches("\\d{6}"),
                eq(java.time.Duration.ofMinutes(5))
        );
    }

    private User saveUser(String email, String password) {
        return userRepository.saveAndFlush(User.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .nickname("Minter")
                .profileImageCode(ProfileImageCode.PROFILE_01)
                .onboardingStatus(OnboardingStatus.INTEREST_SELECTION)
                .lastLoginAt(LocalDateTime.now().minusDays(1))
                .build());
    }

    private String validLoginRequest() {
        return """
                {
                  "email": "user@example.com",
                  "password": "Password123!"
                }
                """;
    }

    private JsonNode login() throws Exception {
        String response = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validLoginRequest()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).path("data");
    }

    private String validSignUpRequest() {
        return """
                {
                  "email": "user@example.com",
                  "password": "Password123!",
                  "nickname": "Minter",
                  "agreements": [
                    {
                      "agreementCode": "TERMS_OF_SERVICE",
                      "version": "1.0",
                      "agreed": true
                    },
                    {
                      "agreementCode": "PRIVACY_POLICY",
                      "version": "1.0",
                      "agreed": true
                    }
                  ]
                }
                """;
    }

    private String validNewKakaoLoginRequest() {
        return """
                {
                  "kakaoAccessToken": "valid-kakao-token",
                  "nickname": "Minter",
                  "agreements": [
                    {
                      "agreementCode": "TERMS_OF_SERVICE",
                      "version": "1.0",
                      "agreed": true
                    },
                    {
                      "agreementCode": "PRIVACY_POLICY",
                      "version": "1.0",
                      "agreed": true
                    }
                  ]
                }
                """;
    }

    private String validNewAppleLoginRequest() {
        return """
                {
                  "identityToken": "valid-apple-identity-token",
                  "authorizationCode": "valid-apple-authorization-code",
                  "nonce": "%s",
                  "nickname": "Minter",
                  "agreements": [
                    {
                      "agreementCode": "TERMS_OF_SERVICE",
                      "version": "1.0",
                      "agreed": true
                    },
                    {
                      "agreementCode": "PRIVACY_POLICY",
                      "version": "1.0",
                      "agreed": true
                    }
                  ]
                }
                """.formatted(APPLE_RAW_NONCE);
    }

    private record RefreshTokenBody(String refreshToken) {
    }

    private void registerPushToken(String accessToken, String deviceId, String fcmToken) throws Exception {
        mockMvc.perform(post("/users/me/push-tokens/{deviceId}", deviceId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fcmToken": "%s",
                                  "platform": "IOS"
                                }
                                """.formatted(fcmToken)))
                .andExpect(status().isOk());
    }
}

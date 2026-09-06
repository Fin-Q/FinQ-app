package com.swyp.FinQ.user.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swyp.FinQ.support.MySqlContainerSupport;
import com.swyp.FinQ.user.domain.OnboardingStatus;
import com.swyp.FinQ.user.domain.ProfileImageCode;
import com.swyp.FinQ.user.domain.RefreshToken;
import com.swyp.FinQ.user.domain.User;
import com.swyp.FinQ.user.domain.UserAgreement;
import com.swyp.FinQ.user.repository.RefreshTokenRepository;
import com.swyp.FinQ.user.repository.UserAgreementRepository;
import com.swyp.FinQ.user.repository.UserRepository;
import com.swyp.FinQ.user.service.TokenHashEncoder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerTest extends MySqlContainerSupport {

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
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TokenHashEncoder tokenHashEncoder;

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
                .andExpect(jsonPath("$.data.onboardingStatus").value("INTEREST_SECTION"))
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
                .onboardingStatus(OnboardingStatus.INTEREST_SECTION)
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
                .andExpect(jsonPath("$.data.onboardingStatus").value("INTEREST_SECTION"))
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

    private User saveUser(String email, String password) {
        return userRepository.saveAndFlush(User.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .nickname("Minter")
                .profileImageCode(ProfileImageCode.PROFILE_01)
                .onboardingStatus(OnboardingStatus.INTEREST_SECTION)
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

    private record RefreshTokenBody(String refreshToken) {
    }
}

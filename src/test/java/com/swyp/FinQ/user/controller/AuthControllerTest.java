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
}

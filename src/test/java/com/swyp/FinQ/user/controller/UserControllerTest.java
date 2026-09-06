package com.swyp.FinQ.user.controller;

import com.swyp.FinQ.content.domain.CategoryCode;
import com.swyp.FinQ.global.security.token.IssuedTokenPair;
import com.swyp.FinQ.global.security.token.JwtTokenProvider;
import com.swyp.FinQ.support.MySqlContainerSupport;
import com.swyp.FinQ.user.domain.OnboardingStatus;
import com.swyp.FinQ.user.domain.ProfileImageCode;
import com.swyp.FinQ.user.domain.User;
import com.swyp.FinQ.user.repository.UserInterestRepository;
import com.swyp.FinQ.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserControllerTest extends MySqlContainerSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserInterestRepository userInterestRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void getsOnboardingStatusAndInterests() throws Exception {
        User user = saveUser();

        mockMvc.perform(get("/users/me/onboarding")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(user.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("온보딩 상태 조회에 성공했습니다."))
                .andExpect(jsonPath("$.data.onboardingStatus").value("INTEREST_SECTION"))
                .andExpect(jsonPath("$.data.interests").isEmpty());
    }

    @Test
    void savesInitialInterestsAndMovesToCharacterGuide() throws Exception {
        User user = saveUser();

        mockMvc.perform(post("/users/me/interests")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(user.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "categoryCodes": ["INV", "SAL"]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("관심 주제 저장에 성공했습니다."))
                .andExpect(jsonPath("$.data.onboardingStatus").value("CHARACTER_GUIDE"))
                .andExpect(jsonPath("$.data.interests.length()").value(2))
                .andExpect(jsonPath("$.data.interests[0].categoryCode").value("SAL"))
                .andExpect(jsonPath("$.data.interests[1].categoryCode").value("INV"));

        User updatedUser = userRepository.findById(user.getId()).orElseThrow();
        assertThat(updatedUser.getOnboardingStatus()).isEqualTo(OnboardingStatus.CHARACTER_GUIDE);
        assertThat(userInterestRepository.findAllByUserId(user.getId())).hasSize(2);
    }

    @Test
    void rejectsRepeatedInitialInterestSelection() throws Exception {
        User user = saveUser();
        String request = """
                {
                  "categoryCodes": ["SAL"]
                }
                """;

        mockMvc.perform(post("/users/me/interests")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(user.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/users/me/interests")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(user.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("USER_INTEREST_ALREADY_SELECTED"));
    }

    @Test
    void rejectsDuplicateInterestCategories() throws Exception {
        User user = saveUser();

        mockMvc.perform(post("/users/me/interests")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(user.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "categoryCodes": ["SAL", "SAL"]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("USER_DUPLICATE_INTEREST_CATEGORY"));
    }

    @Test
    void replacesSelectedInterests() throws Exception {
        User user = saveUser();
        selectInterests(user.getId(), "[\"SAL\", \"INV\"]");

        mockMvc.perform(put("/users/me/interests")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(user.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "categoryCodes": ["TAX", "STK"]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("관심 주제 수정에 성공했습니다."))
                .andExpect(jsonPath("$.data.onboardingStatus").value("CHARACTER_GUIDE"))
                .andExpect(jsonPath("$.data.interests.length()").value(2))
                .andExpect(jsonPath("$.data.interests[0].categoryCode").value("STK"))
                .andExpect(jsonPath("$.data.interests[1].categoryCode").value("TAX"));

        assertThat(userInterestRepository.findAllWithCategoryByUserId(user.getId()))
                .extracting(interest -> interest.getCategory().getCategoryCode())
                .containsExactly(
                        CategoryCode.STK,
                        CategoryCode.TAX
                );
    }

    @Test
    void rejectsInterestUpdateBeforeInitialSelection() throws Exception {
        User user = saveUser();

        mockMvc.perform(put("/users/me/interests")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(user.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "categoryCodes": ["SAL"]
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("USER_INTEREST_NOT_SELECTED"));
    }

    @Test
    void completesOnboardingAndKeepsCompletionTimeOnRepeatedRequest() throws Exception {
        User user = saveUser();
        selectInterests(user.getId(), "[\"SAL\"]");

        mockMvc.perform(patch("/users/me/onboarding/complete")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(user.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("온보딩 완료 처리에 성공했습니다."))
                .andExpect(jsonPath("$.data.onboardingStatus").value("COMPLETED"));

        User completedUser = userRepository.findById(user.getId()).orElseThrow();
        assertThat(completedUser.getOnboardingCompletedAt()).isNotNull();
        var completedAt = completedUser.getOnboardingCompletedAt();

        mockMvc.perform(patch("/users/me/onboarding/complete")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(user.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.onboardingStatus").value("COMPLETED"));

        assertThat(userRepository.findById(user.getId()).orElseThrow().getOnboardingCompletedAt())
                .isEqualTo(completedAt);
    }

    @Test
    void rejectsOnboardingCompletionBeforeInterestSelection() throws Exception {
        User user = saveUser();

        mockMvc.perform(patch("/users/me/onboarding/complete")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(user.getId())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("USER_ONBOARDING_INTEREST_REQUIRED"));
    }

    @Test
    void rejectsOnboardingRequestWithoutAccessToken() throws Exception {
        mockMvc.perform(get("/users/me/onboarding"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("AUTH_UNAUTHORIZED"));
    }

    private User saveUser() {
        return userRepository.saveAndFlush(User.builder()
                .email("onboarding@example.com")
                .password("encoded-password")
                .nickname("Minter")
                .profileImageCode(ProfileImageCode.PROFILE_01)
                .onboardingStatus(OnboardingStatus.INTEREST_SECTION)
                .build());
    }

    private String bearerToken(Long userId) {
        IssuedTokenPair tokens = jwtTokenProvider.issue(userId);
        return "Bearer " + tokens.accessToken();
    }

    private void selectInterests(Long userId, String categoryCodes) throws Exception {
        mockMvc.perform(post("/users/me/interests")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"categoryCodes\":" + categoryCodes + "}"))
                .andExpect(status().isCreated());
    }
}

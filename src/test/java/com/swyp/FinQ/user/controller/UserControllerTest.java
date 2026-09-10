package com.swyp.FinQ.user.controller;

import com.swyp.FinQ.content.domain.Category;
import com.swyp.FinQ.content.domain.CategoryCode;
import com.swyp.FinQ.content.domain.Content;
import com.swyp.FinQ.content.repository.CategoryRepository;
import com.swyp.FinQ.content.repository.ContentRepository;
import com.swyp.FinQ.global.security.token.IssuedTokenPair;
import com.swyp.FinQ.global.security.token.JwtTokenProvider;
import com.swyp.FinQ.learning.domain.UserCategoryCompletion;
import com.swyp.FinQ.learning.domain.UserContentCompletion;
import com.swyp.FinQ.learning.repository.UserCategoryCompletionRepository;
import com.swyp.FinQ.learning.repository.UserContentCompletionRepository;
import com.swyp.FinQ.notification.domain.PushPlatform;
import com.swyp.FinQ.notification.domain.PushToken;
import com.swyp.FinQ.notification.repository.PushTokenRepository;
import com.swyp.FinQ.reward.domain.XpHistory;
import com.swyp.FinQ.reward.domain.XpType;
import com.swyp.FinQ.reward.repository.XpHistoryRepository;
import com.swyp.FinQ.streak.config.StreakConfig;
import com.swyp.FinQ.streak.domain.StreakLog;
import com.swyp.FinQ.streak.repository.StreakLogRepository;
import com.swyp.FinQ.support.MySqlContainerSupport;
import com.swyp.FinQ.user.domain.OnboardingStatus;
import com.swyp.FinQ.user.domain.PasswordResetRequest;
import com.swyp.FinQ.user.domain.ProfileImageCode;
import com.swyp.FinQ.user.domain.SocialAccount;
import com.swyp.FinQ.user.domain.SocialProvider;
import com.swyp.FinQ.user.domain.User;
import com.swyp.FinQ.user.domain.UserAgreement;
import com.swyp.FinQ.user.domain.UserInterest;
import com.swyp.FinQ.user.repository.PasswordResetRequestRepository;
import com.swyp.FinQ.user.repository.RefreshTokenRepository;
import com.swyp.FinQ.user.repository.SocialAccountRepository;
import com.swyp.FinQ.user.repository.UserAgreementRepository;
import com.swyp.FinQ.user.repository.UserInterestRepository;
import com.swyp.FinQ.user.repository.UserRepository;
import com.swyp.FinQ.user.service.AuthTokenService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
    private UserAgreementRepository userAgreementRepository;

    @Autowired
    private SocialAccountRepository socialAccountRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordResetRequestRepository passwordResetRequestRepository;

    @Autowired
    private UserContentCompletionRepository userContentCompletionRepository;

    @Autowired
    private UserCategoryCompletionRepository userCategoryCompletionRepository;

    @Autowired
    private PushTokenRepository pushTokenRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ContentRepository contentRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private AuthTokenService authTokenService;

    @Autowired
    private XpHistoryRepository xpHistoryRepository;

    @Autowired
    private StreakLogRepository streakLogRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void withdrawsAuthenticatedUser() throws Exception {
        User user = saveUser();
        Category category = categoryRepository.findByCategoryCode(CategoryCode.SAL).orElseThrow();
        Content content = contentRepository.findByCategoryOrderByDisplayOrder(category).getFirst();
        LocalDateTime now = LocalDateTime.now();

        UserAgreement agreement = userAgreementRepository.save(UserAgreement.builder()
                .user(user)
                .agreementCode("TERMS_OF_SERVICE")
                .agreementVersion("1.0")
                .agreed(true)
                .agreedAt(now)
                .build());
        SocialAccount socialAccount = socialAccountRepository.save(SocialAccount.builder()
                .user(user)
                .provider(SocialProvider.KAKAO)
                .providerUserId("withdrawal-kakao-user")
                .build());
        PasswordResetRequest passwordResetRequest = passwordResetRequestRepository.save(
                PasswordResetRequest.builder()
                        .user(user)
                        .verificationId("withdrawal-verification")
                        .verificationCodeHash("verification-code-hash")
                        .codeExpiresAt(now.plusMinutes(5))
                        .resendAvailableAt(now.plusMinutes(1))
                        .passwordResetTokenHash("withdrawal-reset-token-hash")
                        .tokenExpiresAt(now.plusMinutes(10))
                        .build()
        );
        UserInterest interest = userInterestRepository.save(UserInterest.builder()
                .user(user)
                .category(category)
                .build());
        UserContentCompletion contentCompletion = userContentCompletionRepository.save(
                UserContentCompletion.builder()
                        .user(user)
                        .content(content)
                        .completedAt(now)
                        .xpEarned(10)
                        .build()
        );
        UserCategoryCompletion categoryCompletion = userCategoryCompletionRepository.save(
                UserCategoryCompletion.builder()
                        .user(user)
                        .category(category)
                        .completedAt(now)
                        .xpEarned(40)
                        .build()
        );
        XpHistory xpHistory = xpHistoryRepository.save(XpHistory.builder()
                .user(user)
                .xpAmount(10)
                .xpType(XpType.CONTENT_COMPLETE)
                .referenceId("content:withdrawal-test")
                .build());
        StreakLog streakLog = streakLogRepository.save(StreakLog.builder()
                .user(user)
                .streakDate(LocalDate.now())
                .build());
        PushToken pushToken = pushTokenRepository.save(PushToken.builder()
                .user(user)
                .sessionId("withdrawal-push-session")
                .deviceId("withdrawal-device")
                .fcmToken("withdrawal-fcm-token")
                .fcmTokenHash("a".repeat(64))
                .platform(PushPlatform.IOS)
                .build());
        IssuedTokenPair tokens = authTokenService.issue(user);
        Long refreshTokenId = refreshTokenRepository.findBySessionId(tokens.sessionId()).orElseThrow().getId();

        entityManager.flush();
        entityManager.clear();

        mockMvc.perform(delete("/users/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.accessToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("회원 탈퇴에 성공했습니다."))
                .andExpect(jsonPath("$.data").doesNotExist());

        entityManager.clear();

        assertThat(userRepository.existsById(user.getId())).isFalse();
        assertThat(userAgreementRepository.existsById(agreement.getId())).isFalse();
        assertThat(socialAccountRepository.existsById(socialAccount.getId())).isFalse();
        assertThat(refreshTokenRepository.existsById(refreshTokenId)).isFalse();
        assertThat(passwordResetRequestRepository.existsById(passwordResetRequest.getId())).isFalse();
        assertThat(userInterestRepository.existsById(interest.getId())).isFalse();
        assertThat(userContentCompletionRepository.existsById(contentCompletion.getId())).isFalse();
        assertThat(userCategoryCompletionRepository.existsById(categoryCompletion.getId())).isFalse();
        assertThat(xpHistoryRepository.existsById(xpHistory.getId())).isFalse();
        assertThat(streakLogRepository.existsById(streakLog.getId())).isFalse();
        assertThat(pushTokenRepository.existsById(pushToken.getId())).isFalse();

        mockMvc.perform(post("/auth/token/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + tokens.refreshToken() + "\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("AUTH_INVALID_REFRESH_TOKEN"));
    }

    @Test
    void rejectsWithdrawalWithoutAccessToken() throws Exception {
        User user = saveUser();

        mockMvc.perform(delete("/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("AUTH_UNAUTHORIZED"));

        assertThat(userRepository.existsById(user.getId())).isTrue();
    }

    @Test
    void returnsNotFoundWhenWithdrawalUserDoesNotExist() throws Exception {
        mockMvc.perform(delete("/users/me")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(Long.MAX_VALUE)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("USER_NOT_FOUND"));
    }

    @Test
    void getsOnboardingStatusAndInterests() throws Exception {
        User user = saveUser();

        mockMvc.perform(get("/users/me/onboarding")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(user.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("온보딩 상태 조회에 성공했습니다."))
                .andExpect(jsonPath("$.data.onboardingStatus").value("INTEREST_SELECTION"))
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
                                  "interestTopicIds": [2, 1]
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
                  "interestTopicIds": [1]
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
                                  "interestTopicIds": [1, 1]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("USER_DUPLICATE_INTEREST_CATEGORY"));
    }

    @Test
    void rejectsMoreThanTwoInterestTopics() throws Exception {
        User user = saveUser();

        mockMvc.perform(post("/users/me/interests")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(user.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "interestTopicIds": [1, 2, 3]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("COMMON_VALIDATION_ERROR"));
    }

    @Test
    void rejectsUnknownInterestTopicId() throws Exception {
        User user = saveUser();

        mockMvc.perform(post("/users/me/interests")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(user.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "interestTopicIds": [999999]
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("CATEGORY_NOT_FOUND"));
    }

    @Test
    void replacesSelectedInterests() throws Exception {
        User user = saveUser();
        selectInterests(user.getId(), "[1, 2]");

        mockMvc.perform(put("/users/me/interests")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(user.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "interestTopicIds": [4, 3]
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
                                  "interestTopicIds": [1]
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("USER_INTEREST_NOT_SELECTED"));
    }

    @Test
    void completesOnboardingAndKeepsCompletionTimeOnRepeatedRequest() throws Exception {
        User user = saveUser();
        selectInterests(user.getId(), "[1]");

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
    void getsMyPageWithProfileAndLearningSummary() throws Exception {
        User user = saveUser();
        selectInterests(user.getId(), "[1]");
        user.addXp(80);
        xpHistoryRepository.save(XpHistory.builder()
                .user(user)
                .xpAmount(80)
                .xpType(XpType.CONTENT_COMPLETE)
                .referenceId("content:my-page-test")
                .build());
        LocalDate today = LocalDate.now(StreakConfig.STREAK_ZONE_ID);
        saveStreakLogs(user, today.minusDays(2), today.minusDays(1), today);

        mockMvc.perform(get("/users/me")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(user.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("마이페이지 조회에 성공했습니다."))
                .andExpect(jsonPath("$.data.userId").value(String.valueOf(user.getId())))
                .andExpect(jsonPath("$.data.email").value("onboarding@example.com"))
                .andExpect(jsonPath("$.data.nickname").value("Minter"))
                .andExpect(jsonPath("$.data.profileImageCode").value("PROFILE_01"))
                .andExpect(jsonPath("$.data.totalXp").value(80))
                .andExpect(jsonPath("$.data.level").doesNotExist())
                .andExpect(jsonPath("$.data.currentStreakDays").value(3))
                .andExpect(jsonPath("$.data.notificationEnabled").value(true))
                .andExpect(jsonPath("$.data.interests[0].categoryCode").value("SAL"));
    }

    @Test
    void updatesNicknameWithoutChangingProfileImage() throws Exception {
        User user = saveUser();

        mockMvc.perform(patch("/users/me/nickname")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(user.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nickname": "  핀큐  "
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("닉네임 변경에 성공했습니다."))
                .andExpect(jsonPath("$.data.nickname").value("핀큐"))
                .andExpect(jsonPath("$.data.updatedAt").value(org.hamcrest.Matchers.endsWith("+09:00")))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data.profileImageCode").doesNotExist());

        User updatedUser = userRepository.findById(user.getId()).orElseThrow();
        assertThat(updatedUser.getNickname()).isEqualTo("핀큐");
        assertThat(updatedUser.getProfileImageCode()).isEqualTo(ProfileImageCode.PROFILE_01);
    }

    @Test
    void updatesProfileImageWithoutChangingNickname() throws Exception {
        User user = saveUser();

        mockMvc.perform(patch("/users/me/profile-image")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(user.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "profileImageCode": "PROFILE_04"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("프로필 이미지 변경에 성공했습니다."))
                .andExpect(jsonPath("$.data.profileImageCode").value("PROFILE_04"))
                .andExpect(jsonPath("$.data.length()").value(1));

        User updatedUser = userRepository.findById(user.getId()).orElseThrow();
        assertThat(updatedUser.getNickname()).isEqualTo("Minter");
        assertThat(updatedUser.getProfileImageCode()).isEqualTo(ProfileImageCode.PROFILE_04);
    }

    @Test
    void rejectsEmptyNicknameUpdate() throws Exception {
        User user = saveUser();

        mockMvc.perform(patch("/users/me/nickname")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(user.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("COMMON_VALIDATION_ERROR"));
    }

    @Test
    void rejectsBlankNickname() throws Exception {
        User user = saveUser();

        mockMvc.perform(patch("/users/me/nickname")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(user.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nickname": "   "
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("COMMON_VALIDATION_ERROR"));
    }

    @Test
    void rejectsOnboardingRequestWithoutAccessToken() throws Exception {
        mockMvc.perform(get("/users/me/onboarding"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("AUTH_UNAUTHORIZED"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"profileImageCode\":null}", "{\"profileImageCode\":\"INVALID\"}"})
    void rejectsInvalidProfileImage(String body) throws Exception {
        User user = saveUser();
        mockMvc.perform(patch("/users/me/profile-image")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(user.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
        assertThat(user.getProfileImageCode()).isEqualTo(ProfileImageCode.PROFILE_01);
    }

    @ParameterizedTest
    @ValueSource(strings = {"{\"nickname\":null}", "{\"nickname\":\"\"}"})
    void rejectsInvalidNickname(String body) throws Exception {
        User user = saveUser();
        mockMvc.perform(patch("/users/me/nickname")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(user.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
        assertThat(user.getNickname()).isEqualTo("Minter");
    }

    @Test
    void rejectsNicknameOverFifteenCharacters() throws Exception {
        User user = saveUser();
        mockMvc.perform(patch("/users/me/nickname")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(user.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nickname\":\"" + "a".repeat(16) + "\"}"))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/users/me/nickname", "/users/me/profile-image"})
    void rejectsProfileChangesWithoutAccessToken(String path) throws Exception {
        mockMvc.perform(patch(path)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    private User saveUser() {
        return userRepository.saveAndFlush(User.builder()
                .email("onboarding@example.com")
                .password("encoded-password")
                .nickname("Minter")
                .profileImageCode(ProfileImageCode.PROFILE_01)
                .onboardingStatus(OnboardingStatus.INTEREST_SELECTION)
                .currentStreak(3)
                .build());
    }

    private String bearerToken(Long userId) {
        IssuedTokenPair tokens = jwtTokenProvider.issue(userId);
        return "Bearer " + tokens.accessToken();
    }

    private void saveStreakLogs(User user, LocalDate... streakDates) {
        for (LocalDate streakDate : streakDates) {
            streakLogRepository.save(StreakLog.builder()
                    .user(user)
                    .streakDate(streakDate)
                    .build());
        }
    }

    private void selectInterests(Long userId, String interestTopicIds) throws Exception {
        mockMvc.perform(post("/users/me/interests")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"interestTopicIds\":" + interestTopicIds + "}"))
                .andExpect(status().isCreated());
    }
}

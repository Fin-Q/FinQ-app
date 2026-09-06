package com.swyp.FinQ.user.repository;

import com.swyp.FinQ.content.domain.Category;
import com.swyp.FinQ.content.domain.CategoryCode;
import com.swyp.FinQ.content.repository.CategoryRepository;
import com.swyp.FinQ.global.config.JpaAuditingConfig;
import com.swyp.FinQ.support.MySqlContainerSupport;
import com.swyp.FinQ.user.domain.OnboardingStatus;
import com.swyp.FinQ.user.domain.PasswordResetRequest;
import com.swyp.FinQ.user.domain.ProfileImageCode;
import com.swyp.FinQ.user.domain.RefreshToken;
import com.swyp.FinQ.user.domain.SocialAccount;
import com.swyp.FinQ.user.domain.SocialProvider;
import com.swyp.FinQ.user.domain.User;
import com.swyp.FinQ.user.domain.UserAgreement;
import com.swyp.FinQ.user.domain.UserInterest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaAuditingConfig.class)
class UserRepositoryTest extends MySqlContainerSupport {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SocialAccountRepository socialAccountRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserAgreementRepository userAgreementRepository;

    @Autowired
    private UserInterestRepository userInterestRepository;

    @Autowired
    private PasswordResetRequestRepository passwordResetRequestRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    @DisplayName("이메일로 사용자를 조회한다")
    void findUserByEmail() {
        User user = userRepository.save(createUser());

        User found = userRepository.findByEmail(user.getEmail()).orElseThrow();

        assertThat(found.getId()).isEqualTo(user.getId());
        assertThat(found.getOnboardingStatus()).isEqualTo(OnboardingStatus.INTEREST_SELECTION);
        assertThat(userRepository.existsByEmail(user.getEmail())).isTrue();
    }

    @Test
    @DisplayName("사용자의 소셜 계정과 리프레시 토큰을 조회한다")
    void findAuthenticationInformation() {
        User user = userRepository.save(createUser());
        SocialAccount socialAccount = socialAccountRepository.save(SocialAccount.builder()
                .user(user)
                .provider(SocialProvider.APPLE)
                .providerUserId("apple-user-id")
                .build());
        RefreshToken refreshToken = refreshTokenRepository.save(RefreshToken.builder()
                .user(user)
                .token("refresh-token")
                .expiresAt(LocalDateTime.now().plusDays(14))
                .build());

        SocialAccount foundSocialAccount = socialAccountRepository
                .findByProviderAndProviderUserId(SocialProvider.APPLE, "apple-user-id")
                .orElseThrow();
        RefreshToken foundRefreshToken = refreshTokenRepository.findByToken("refresh-token").orElseThrow();

        assertThat(foundSocialAccount.getId()).isEqualTo(socialAccount.getId());
        assertThat(foundRefreshToken.getId()).isEqualTo(refreshToken.getId());
        assertThat(socialAccountRepository.existsByUserIdAndProvider(user.getId(), SocialProvider.APPLE)).isTrue();
    }

    @Test
    @DisplayName("사용자의 약관 동의 내역을 조회한다")
    void findUserAgreement() {
        User user = userRepository.save(createUser());
        LocalDateTime agreedAt = user.getCreatedAt();
        UserAgreement agreement = userAgreementRepository.save(UserAgreement.builder()
                .user(user)
                .agreementCode("TERMS_OF_SERVICE")
                .agreementVersion("1.0")
                .agreed(true)
                .agreedAt(agreedAt)
                .build());

        UserAgreement found = userAgreementRepository.findAllByUserId(user.getId()).getFirst();

        assertThat(found.getId()).isEqualTo(agreement.getId());
        assertThat(found.isAgreed()).isTrue();
        assertThat(found.getAgreedAt()).isEqualTo(user.getCreatedAt());
        assertThat(userAgreementRepository.existsByUserIdAndAgreementCodeAndAgreementVersion(
                user.getId(),
                "TERMS_OF_SERVICE",
                "1.0"
        )).isTrue();
    }

    @Test
    @DisplayName("사용자의 관심 주제를 조회한다")
    void findUserInterest() {
        User user = userRepository.save(createUser());
        Category category = categoryRepository.findByCategoryCode(CategoryCode.SAL).orElseThrow();
        UserInterest interest = userInterestRepository.save(UserInterest.builder()
                .user(user)
                .category(category)
                .build());

        UserInterest found = userInterestRepository.findAllByUserId(user.getId()).getFirst();

        assertThat(found.getId()).isEqualTo(interest.getId());
        assertThat(found.getCategory().getId()).isEqualTo(category.getId());
    }

    @Test
    @DisplayName("비밀번호 재설정 요청을 인증 ID와 재설정 토큰으로 조회한다")
    void findPasswordResetRequest() {
        User user = userRepository.save(createUser());
        PasswordResetRequest request = passwordResetRequestRepository.save(PasswordResetRequest.builder()
                .user(user)
                .verificationId("verification-id")
                .verificationCodeHash("verification-code-hash")
                .codeExpiresAt(LocalDateTime.now().plusMinutes(5))
                .resendAvailableAt(LocalDateTime.now().plusMinutes(1))
                .passwordResetTokenHash("password-reset-token-hash")
                .tokenExpiresAt(LocalDateTime.now().plusMinutes(10))
                .build());

        PasswordResetRequest foundByVerificationId = passwordResetRequestRepository
                .findByVerificationId("verification-id")
                .orElseThrow();
        PasswordResetRequest foundByToken = passwordResetRequestRepository
                .findByPasswordResetTokenHash("password-reset-token-hash")
                .orElseThrow();

        assertThat(foundByVerificationId.getId()).isEqualTo(request.getId());
        assertThat(foundByToken.getId()).isEqualTo(request.getId());
    }

    private User createUser() {
        return User.builder()
                .email("user@example.com")
                .password("encoded-password")
                .nickname("Minter")
                .profileImageCode(ProfileImageCode.PROFILE_01)
                .build();
    }
}

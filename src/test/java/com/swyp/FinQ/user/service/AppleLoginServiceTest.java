package com.swyp.FinQ.user.service;

import com.swyp.FinQ.global.exception.BaseException;
import com.swyp.FinQ.global.security.token.IssuedTokenPair;
import com.swyp.FinQ.user.domain.OnboardingStatus;
import com.swyp.FinQ.user.domain.ProfileImageCode;
import com.swyp.FinQ.user.domain.SocialAccount;
import com.swyp.FinQ.user.domain.SocialProvider;
import com.swyp.FinQ.user.domain.User;
import com.swyp.FinQ.user.dto.req.AgreementRequest;
import com.swyp.FinQ.user.exception.AuthErrorCode;
import com.swyp.FinQ.user.repository.SocialAccountRepository;
import com.swyp.FinQ.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AppleLoginServiceTest {

    private static final Instant NOW = Instant.parse("2026-09-08T03:00:00Z");
    private static final String PROVIDER_USER_ID = "apple-user-id";
    private static final String IDENTITY_TOKEN = "apple-identity-token";
    private static final String AUTHORIZATION_CODE = "apple-authorization-code";
    private static final String NONCE = "raw-nonce";

    @Mock
    private AppleIdentityTokenVerifier appleIdentityTokenVerifier;

    @Mock
    private AppleAuthorizationCodeVerifier appleAuthorizationCodeVerifier;

    @Mock
    private SocialAccountRepository socialAccountRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AgreementRegistrationService agreementRegistrationService;

    @Mock
    private AuthTokenService authTokenService;

    private AppleLoginService appleLoginService;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(NOW, ZoneId.of("Asia/Seoul"));
        appleLoginService = new AppleLoginService(
                appleIdentityTokenVerifier,
                appleAuthorizationCodeVerifier,
                socialAccountRepository,
                userRepository,
                agreementRegistrationService,
                authTokenService,
                clock
        );
    }

    @Test
    void 기존_Apple_회원은_신규_가입_정보_없이_로그인한다() {
        User user = user(1L, "기존회원");
        SocialAccount account = SocialAccount.builder()
                .user(user)
                .provider(SocialProvider.APPLE)
                .providerUserId(PROVIDER_USER_ID)
                .build();
        mockVerifiedIdentity();
        given(socialAccountRepository.findByProviderAndProviderUserId(SocialProvider.APPLE, PROVIDER_USER_ID))
                .willReturn(Optional.of(account));
        given(authTokenService.issue(user)).willReturn(tokens());

        AppleLoginResult result = appleLoginService.login(command(null, null));

        assertThat(result.userId()).isEqualTo("1");
        assertThat(result.nickname()).isEqualTo("기존회원");
        assertThat(result.isNewUser()).isFalse();
        assertThat(result.accessToken()).isEqualTo("finq-access-token");
        assertThat(user.getLastLoginAt()).isEqualTo("2026-09-08T12:00:00");
        verify(userRepository, never()).saveAndFlush(any(User.class));
        verify(agreementRegistrationService, never()).validateRequired(any());
    }

    @Test
    void 신규_Apple_회원과_약관과_소셜_계정을_저장하고_로그인한다() {
        List<AgreementRequest> agreements = requiredAgreements();
        User savedUser = user(2L, "신규회원");
        mockVerifiedIdentity();
        given(socialAccountRepository.findByProviderAndProviderUserId(SocialProvider.APPLE, PROVIDER_USER_ID))
                .willReturn(Optional.empty());
        given(userRepository.saveAndFlush(any(User.class))).willReturn(savedUser);
        given(authTokenService.issue(savedUser)).willReturn(tokens());

        AppleLoginResult result = appleLoginService.login(command("신규회원", agreements));

        assertThat(result.userId()).isEqualTo("2");
        assertThat(result.isNewUser()).isTrue();
        assertThat(result.refreshToken()).isEqualTo("finq-refresh-token");
        assertThat(result.onboardingStatus()).isEqualTo(OnboardingStatus.INTEREST_SECTION);
        assertThat(savedUser.getLastLoginAt()).isEqualTo("2026-09-08T12:00:00");
        verify(agreementRegistrationService).validateRequired(agreements);
        verify(agreementRegistrationService).save(savedUser, agreements, savedUser.getLastLoginAt());

        ArgumentCaptor<SocialAccount> accountCaptor = ArgumentCaptor.forClass(SocialAccount.class);
        verify(socialAccountRepository).save(accountCaptor.capture());
        assertThat(accountCaptor.getValue().getProvider()).isEqualTo(SocialProvider.APPLE);
        assertThat(accountCaptor.getValue().getProviderUserId()).isEqualTo(PROVIDER_USER_ID);
        assertThat(accountCaptor.getValue().getUser()).isSameAs(savedUser);
    }

    @Test
    void Identity_Token_검증_후_Authorization_Code를_검증한다() {
        User user = user(1L, "기존회원");
        mockVerifiedIdentity();
        given(socialAccountRepository.findByProviderAndProviderUserId(SocialProvider.APPLE, PROVIDER_USER_ID))
                .willReturn(Optional.of(SocialAccount.builder()
                        .user(user)
                        .provider(SocialProvider.APPLE)
                        .providerUserId(PROVIDER_USER_ID)
                        .build()));
        given(authTokenService.issue(user)).willReturn(tokens());

        appleLoginService.login(command(null, null));

        InOrder verificationOrder = inOrder(appleIdentityTokenVerifier, appleAuthorizationCodeVerifier);
        verificationOrder.verify(appleIdentityTokenVerifier).verify(IDENTITY_TOKEN, NONCE);
        verificationOrder.verify(appleAuthorizationCodeVerifier).verify(
                AUTHORIZATION_CODE,
                NONCE,
                new AppleUserIdentity(PROVIDER_USER_ID)
        );
    }

    @Test
    void 신규_Apple_회원의_닉네임이_없으면_거부한다() {
        mockVerifiedIdentity();
        given(socialAccountRepository.findByProviderAndProviderUserId(SocialProvider.APPLE, PROVIDER_USER_ID))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> appleLoginService.login(command(" ", requiredAgreements())))
                .isInstanceOfSatisfying(BaseException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(AuthErrorCode.INVALID_APPLE_SIGN_UP_INFO));

        verify(userRepository, never()).saveAndFlush(any(User.class));
        verify(appleAuthorizationCodeVerifier, never()).verify(any(), any(), any());
    }

    @Test
    void 필수_약관_검증이_실패하면_Apple_회원정보를_저장하지_않는다() {
        List<AgreementRequest> agreements = requiredAgreements();
        BaseException agreementException = BaseException.of(AuthErrorCode.REQUIRED_AGREEMENT_NOT_ACCEPTED);
        mockVerifiedIdentity();
        given(socialAccountRepository.findByProviderAndProviderUserId(SocialProvider.APPLE, PROVIDER_USER_ID))
                .willReturn(Optional.empty());
        org.mockito.BDDMockito.willThrow(agreementException)
                .given(agreementRegistrationService)
                .validateRequired(agreements);

        assertThatThrownBy(() -> appleLoginService.login(command("신규회원", agreements)))
                .isSameAs(agreementException);

        verify(userRepository, never()).saveAndFlush(any(User.class));
        verify(socialAccountRepository, never()).save(any(SocialAccount.class));
        verify(appleAuthorizationCodeVerifier, never()).verify(any(), any(), any());
    }

    private void mockVerifiedIdentity() {
        AppleUserIdentity identity = new AppleUserIdentity(PROVIDER_USER_ID);
        given(appleIdentityTokenVerifier.verify(IDENTITY_TOKEN, NONCE)).willReturn(identity);
    }

    private AppleLoginCommand command(String nickname, List<AgreementRequest> agreements) {
        return new AppleLoginCommand(
                IDENTITY_TOKEN,
                AUTHORIZATION_CODE,
                NONCE,
                nickname,
                agreements
        );
    }

    private User user(Long id, String nickname) {
        return User.builder()
                .id(id)
                .nickname(nickname)
                .profileImageCode(ProfileImageCode.PROFILE_01)
                .onboardingStatus(OnboardingStatus.INTEREST_SECTION)
                .build();
    }

    private List<AgreementRequest> requiredAgreements() {
        return List.of(
                new AgreementRequest("TERMS_OF_SERVICE", "1.0", true),
                new AgreementRequest("PRIVACY_POLICY", "1.0", true)
        );
    }

    private IssuedTokenPair tokens() {
        return new IssuedTokenPair(
                "finq-access-token",
                "finq-refresh-token",
                "session-id",
                3600,
                1209600,
                NOW.plusSeconds(1209600)
        );
    }
}

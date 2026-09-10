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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class KakaoLoginServiceTest {

    private static final Instant NOW = Instant.parse("2026-09-08T03:00:00Z");
    private static final String PROVIDER_USER_ID = "123456789";

    @Mock
    private KakaoAccessTokenVerifier kakaoAccessTokenVerifier;

    @Mock
    private SocialAccountRepository socialAccountRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AgreementRegistrationService agreementRegistrationService;

    @Mock
    private AuthTokenService authTokenService;

    private KakaoLoginService kakaoLoginService;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(NOW, ZoneId.of("Asia/Seoul"));
        kakaoLoginService = new KakaoLoginService(
                kakaoAccessTokenVerifier,
                socialAccountRepository,
                userRepository,
                agreementRegistrationService,
                authTokenService,
                clock
        );
    }

    @Test
    void logsInExistingKakaoUserWithoutNewUserInformation() {
        User user = user(1L, "기존회원");
        SocialAccount account = SocialAccount.builder()
                .user(user)
                .provider(SocialProvider.KAKAO)
                .providerUserId(PROVIDER_USER_ID)
                .build();
        given(kakaoAccessTokenVerifier.verify("kakao-token"))
                .willReturn(new KakaoUserIdentity(PROVIDER_USER_ID));
        given(socialAccountRepository.findByProviderAndProviderUserId(SocialProvider.KAKAO, PROVIDER_USER_ID))
                .willReturn(Optional.of(account));
        given(authTokenService.issue(user)).willReturn(tokens());

        KakaoLoginResult result = kakaoLoginService.login(
                new KakaoLoginCommand("kakao-token", null, null)
        );

        assertThat(result.userId()).isEqualTo("1");
        assertThat(result.nickname()).isEqualTo("기존회원");
        assertThat(result.isNewUser()).isFalse();
        assertThat(result.accessToken()).isEqualTo("finq-access-token");
        assertThat(user.getLastLoginAt()).isEqualTo("2026-09-08T12:00:00");
        verify(userRepository, never()).saveAndFlush(any(User.class));
        verify(agreementRegistrationService, never()).validateRequired(any());
    }

    @Test
    void registersAndLogsInNewKakaoUser() {
        List<AgreementRequest> agreements = requiredAgreements();
        User savedUser = user(2L, "신규회원");
        given(kakaoAccessTokenVerifier.verify("kakao-token"))
                .willReturn(new KakaoUserIdentity(PROVIDER_USER_ID));
        given(socialAccountRepository.findByProviderAndProviderUserId(SocialProvider.KAKAO, PROVIDER_USER_ID))
                .willReturn(Optional.empty());
        given(userRepository.saveAndFlush(any(User.class))).willReturn(savedUser);
        given(authTokenService.issue(savedUser)).willReturn(tokens());

        KakaoLoginResult result = kakaoLoginService.login(
                new KakaoLoginCommand("kakao-token", "신규회원", agreements)
        );

        assertThat(result.userId()).isEqualTo("2");
        assertThat(result.isNewUser()).isTrue();
        assertThat(result.onboardingStatus()).isEqualTo(OnboardingStatus.INTEREST_SELECTION);
        assertThat(savedUser.getLastLoginAt()).isEqualTo("2026-09-08T12:00:00");
        verify(agreementRegistrationService).validateRequired(agreements);
        verify(agreementRegistrationService).save(savedUser, agreements, savedUser.getLastLoginAt());

        ArgumentCaptor<SocialAccount> accountCaptor = ArgumentCaptor.forClass(SocialAccount.class);
        verify(socialAccountRepository).save(accountCaptor.capture());
        assertThat(accountCaptor.getValue().getProvider()).isEqualTo(SocialProvider.KAKAO);
        assertThat(accountCaptor.getValue().getProviderUserId()).isEqualTo(PROVIDER_USER_ID);
        assertThat(accountCaptor.getValue().getUser()).isSameAs(savedUser);
    }

    @Test
    void rejectsNewKakaoUserWithoutNickname() {
        given(kakaoAccessTokenVerifier.verify("kakao-token"))
                .willReturn(new KakaoUserIdentity(PROVIDER_USER_ID));
        given(socialAccountRepository.findByProviderAndProviderUserId(SocialProvider.KAKAO, PROVIDER_USER_ID))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> kakaoLoginService.login(
                new KakaoLoginCommand("kakao-token", " ", requiredAgreements())
        ))
                .isInstanceOfSatisfying(BaseException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(AuthErrorCode.INVALID_KAKAO_SIGN_UP_INFO));

        verify(userRepository, never()).saveAndFlush(any(User.class));
    }

    @Test
    void rejectsNewKakaoUserWithNicknameOverFifteenCharacters() {
        given(kakaoAccessTokenVerifier.verify("kakao-token"))
                .willReturn(new KakaoUserIdentity(PROVIDER_USER_ID));
        given(socialAccountRepository.findByProviderAndProviderUserId(SocialProvider.KAKAO, PROVIDER_USER_ID))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> kakaoLoginService.login(
                new KakaoLoginCommand("kakao-token", "a".repeat(16), requiredAgreements())
        ))
                .isInstanceOfSatisfying(BaseException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(AuthErrorCode.INVALID_KAKAO_SIGN_UP_INFO));

        verify(userRepository, never()).saveAndFlush(any(User.class));
    }

    private User user(Long id, String nickname) {
        return User.builder()
                .id(id)
                .nickname(nickname)
                .profileImageCode(ProfileImageCode.PROFILE_01)
                .onboardingStatus(OnboardingStatus.INTEREST_SELECTION)
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

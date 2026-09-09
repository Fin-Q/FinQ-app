package com.swyp.FinQ.user.service;

import com.swyp.FinQ.global.exception.BaseException;
import com.swyp.FinQ.global.security.token.IssuedTokenPair;
import com.swyp.FinQ.user.domain.OnboardingStatus;
import com.swyp.FinQ.user.domain.ProfileImageCode;
import com.swyp.FinQ.user.domain.SocialAccount;
import com.swyp.FinQ.user.domain.SocialProvider;
import com.swyp.FinQ.user.domain.User;
import com.swyp.FinQ.user.exception.AuthErrorCode;
import com.swyp.FinQ.user.repository.SocialAccountRepository;
import com.swyp.FinQ.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AppleLoginService {

    private static final ZoneId SERVICE_ZONE_ID = ZoneId.of("Asia/Seoul");
    private static final int MAX_NICKNAME_LENGTH = 50;

    private final AppleIdentityTokenVerifier appleIdentityTokenVerifier;
    private final AppleAuthorizationCodeVerifier appleAuthorizationCodeVerifier;
    private final SocialAccountRepository socialAccountRepository;
    private final UserRepository userRepository;
    private final AgreementRegistrationService agreementRegistrationService;
    private final AuthTokenService authTokenService;
    private final Clock clock;

    @Transactional
    public AppleLoginResult login(AppleLoginCommand command) {
        AppleUserIdentity identity = appleIdentityTokenVerifier.verify(
                command.identityToken(),
                command.nonce()
        );
        LocalDateTime loginAt = LocalDateTime.ofInstant(clock.instant(), SERVICE_ZONE_ID);

        Optional<SocialAccount> existingAccount = socialAccountRepository
                .findByProviderAndProviderUserId(SocialProvider.APPLE, identity.providerUserId());

        if (existingAccount.isPresent()) {
            verifyAuthorizationCode(command, identity);
            return loginExistingUser(existingAccount.get().getUser(), loginAt);
        }
        return registerAndLoginNewUser(command, identity, loginAt);
    }

    private AppleLoginResult loginExistingUser(User user, LocalDateTime loginAt) {
        user.updateLastLoginAt(loginAt);
        return toResult(user, false, authTokenService.issue(user));
    }

    private AppleLoginResult registerAndLoginNewUser(
            AppleLoginCommand command,
            AppleUserIdentity identity,
            LocalDateTime loginAt
    ) {
        validateNickname(command.nickname());
        agreementRegistrationService.validateRequired(command.agreements());
        verifyAuthorizationCode(command, identity);

        User user = userRepository.saveAndFlush(User.builder()
                .nickname(command.nickname())
                .profileImageCode(ProfileImageCode.random())
                .onboardingStatus(OnboardingStatus.INTEREST_SELECTION)
                .build());
        user.updateLastLoginAt(loginAt);

        socialAccountRepository.save(SocialAccount.builder()
                .user(user)
                .provider(SocialProvider.APPLE)
                .providerUserId(identity.providerUserId())
                .build());
        agreementRegistrationService.save(user, command.agreements(), loginAt);

        return toResult(user, true, authTokenService.issue(user));
    }

    private void verifyAuthorizationCode(AppleLoginCommand command, AppleUserIdentity identity) {
        appleAuthorizationCodeVerifier.verify(
                command.authorizationCode(),
                command.nonce(),
                identity
        );
    }

    private void validateNickname(String nickname) {
        if (nickname == null || nickname.isBlank() || nickname.length() > MAX_NICKNAME_LENGTH) {
            throw BaseException.of(AuthErrorCode.INVALID_APPLE_SIGN_UP_INFO);
        }
    }

    private AppleLoginResult toResult(User user, boolean isNewUser, IssuedTokenPair tokens) {
        return new AppleLoginResult(
                String.valueOf(user.getId()),
                user.getNickname(),
                isNewUser,
                tokens.accessToken(),
                tokens.refreshToken(),
                "Bearer",
                tokens.accessTokenExpiresIn(),
                user.getOnboardingStatus()
        );
    }
}

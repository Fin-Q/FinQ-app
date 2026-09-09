package com.swyp.FinQ.user.service;

import com.swyp.FinQ.global.exception.BaseException;
import com.swyp.FinQ.global.security.token.IssuedTokenPair;
import com.swyp.FinQ.user.domain.OnboardingStatus;
import com.swyp.FinQ.user.domain.ProfileImageCode;
import com.swyp.FinQ.user.domain.User;
import com.swyp.FinQ.user.dto.req.SignUpRequest;
import com.swyp.FinQ.user.dto.res.SignUpResponse;
import com.swyp.FinQ.user.exception.AuthErrorCode;
import com.swyp.FinQ.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SignUpService {

    private final UserRepository userRepository;
    private final AgreementRegistrationService agreementRegistrationService;
    private final PasswordEncoder passwordEncoder;
    private final AuthTokenService authTokenService;

    @Transactional
    public SignUpResponse signUp(SignUpRequest request) {
        validateEmail(request.email());
        agreementRegistrationService.validateRequired(request.agreements());

        User user = userRepository.saveAndFlush(User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .nickname(request.nickname())
                .profileImageCode(ProfileImageCode.random())
                .onboardingStatus(OnboardingStatus.INTEREST_SELECTION)
                .build());

        LocalDateTime accountCreatedAt = user.getCreatedAt();
        user.updateLastLoginAt(accountCreatedAt);
        agreementRegistrationService.save(user, request.agreements(), accountCreatedAt);
        IssuedTokenPair tokens = authTokenService.issue(user);

        return new SignUpResponse(
                String.valueOf(user.getId()),
                tokens.accessToken(),
                tokens.refreshToken(),
                "Bearer",
                tokens.accessTokenExpiresIn(),
                user.getOnboardingStatus()
        );
    }

    private void validateEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw BaseException.of(AuthErrorCode.EMAIL_ALREADY_EXISTS);
        }
    }

}

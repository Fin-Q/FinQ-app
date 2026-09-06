package com.swyp.FinQ.user.service;

import com.swyp.FinQ.global.exception.BaseException;
import com.swyp.FinQ.global.security.token.IssuedTokenPair;
import com.swyp.FinQ.user.domain.OnboardingStatus;
import com.swyp.FinQ.user.domain.ProfileImageCode;
import com.swyp.FinQ.user.domain.User;
import com.swyp.FinQ.user.domain.UserAgreement;
import com.swyp.FinQ.user.dto.req.AgreementRequest;
import com.swyp.FinQ.user.dto.req.SignUpRequest;
import com.swyp.FinQ.user.dto.res.SignUpResponse;
import com.swyp.FinQ.user.exception.AuthErrorCode;
import com.swyp.FinQ.user.repository.UserAgreementRepository;
import com.swyp.FinQ.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SignUpService {

    private static final Set<String> REQUIRED_AGREEMENT_CODES = Set.of(
            "TERMS_OF_SERVICE",
            "PRIVACY_POLICY"
    );

    private final UserRepository userRepository;
    private final UserAgreementRepository userAgreementRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthTokenService authTokenService;

    @Transactional
    public SignUpResponse signUp(SignUpRequest request) {
        validateEmail(request.email());
        validateAgreements(request.agreements());

        User user = userRepository.saveAndFlush(User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .nickname(request.nickname())
                .profileImageCode(ProfileImageCode.random())
                .onboardingStatus(OnboardingStatus.INTEREST_SECTION)
                .build());

        LocalDateTime accountCreatedAt = user.getCreatedAt();
        user.updateLastLoginAt(accountCreatedAt);
        saveAgreements(user, request.agreements(), accountCreatedAt);
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

    private void validateAgreements(List<AgreementRequest> agreements) {
        Set<String> agreementCodes = agreements.stream()
                .map(AgreementRequest::agreementCode)
                .collect(Collectors.toSet());
        if (!agreementCodes.containsAll(REQUIRED_AGREEMENT_CODES)) {
            throw BaseException.of(AuthErrorCode.REQUIRED_AGREEMENT_MISSING);
        }

        boolean requiredAgreementRejected = agreements.stream()
                .anyMatch(agreement -> !agreement.agreed());
        if (requiredAgreementRejected) {
            throw BaseException.of(AuthErrorCode.REQUIRED_AGREEMENT_NOT_ACCEPTED);
        }
    }

    private void saveAgreements(User user, List<AgreementRequest> agreements, LocalDateTime agreedAt) {
        List<UserAgreement> userAgreements = agreements.stream()
                .map(agreement -> UserAgreement.builder()
                        .user(user)
                        .agreementCode(agreement.agreementCode())
                        .agreementVersion(agreement.version())
                        .agreed(agreement.agreed())
                        .agreedAt(agreedAt)
                        .build())
                .toList();
        userAgreementRepository.saveAll(userAgreements);
    }
}

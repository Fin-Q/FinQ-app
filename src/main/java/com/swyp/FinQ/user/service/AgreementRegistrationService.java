package com.swyp.FinQ.user.service;

import com.swyp.FinQ.global.exception.BaseException;
import com.swyp.FinQ.user.domain.AgreementPolicy;
import com.swyp.FinQ.user.domain.User;
import com.swyp.FinQ.user.domain.UserAgreement;
import com.swyp.FinQ.user.dto.req.AgreementRequest;
import com.swyp.FinQ.user.exception.AuthErrorCode;
import com.swyp.FinQ.user.repository.UserAgreementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AgreementRegistrationService {

    private static final Set<String> REQUIRED_AGREEMENT_CODES = Arrays.stream(AgreementPolicy.values())
            .filter(AgreementPolicy::isRequired)
            .map(Enum::name)
            .collect(Collectors.toUnmodifiableSet());

    private final UserAgreementRepository userAgreementRepository;

    public void validateRequired(List<AgreementRequest> agreements) {
        if (agreements == null || agreements.isEmpty()) {
            throw BaseException.of(AuthErrorCode.REQUIRED_AGREEMENT_MISSING);
        }

        Set<String> acceptedAgreementCodes = agreements.stream()
                .filter(agreement -> Boolean.TRUE.equals(agreement.agreed()))
                .map(AgreementRequest::agreementCode)
                .collect(Collectors.toSet());

        Set<String> submittedAgreementCodes = agreements.stream()
                .map(AgreementRequest::agreementCode)
                .collect(Collectors.toSet());

        if (!submittedAgreementCodes.containsAll(REQUIRED_AGREEMENT_CODES)) {
            throw BaseException.of(AuthErrorCode.REQUIRED_AGREEMENT_MISSING);
        }
        if (!acceptedAgreementCodes.containsAll(REQUIRED_AGREEMENT_CODES)) {
            throw BaseException.of(AuthErrorCode.REQUIRED_AGREEMENT_NOT_ACCEPTED);
        }
    }

    public void save(User user, List<AgreementRequest> agreements, LocalDateTime agreedAt) {
        List<UserAgreement> userAgreements = agreements.stream()
                .map(agreement -> UserAgreement.builder()
                        .user(user)
                        .agreementCode(agreement.agreementCode())
                        .agreementVersion(agreement.version())
                        .agreed(Boolean.TRUE.equals(agreement.agreed()))
                        .agreedAt(agreedAt)
                        .build())
                .toList();
        userAgreementRepository.saveAll(userAgreements);
    }
}

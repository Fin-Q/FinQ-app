package com.swyp.FinQ.user.service;

import com.swyp.FinQ.global.exception.BaseException;
import com.swyp.FinQ.user.dto.req.AgreementRequest;
import com.swyp.FinQ.user.exception.AuthErrorCode;
import com.swyp.FinQ.user.repository.UserAgreementRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class AgreementRegistrationServiceTest {

    private final AgreementRegistrationService service = new AgreementRegistrationService(
            mock(UserAgreementRepository.class)
    );

    @Test
    void rejectsMissingRequiredAgreement() {
        List<AgreementRequest> agreements = List.of(
                new AgreementRequest("TERMS_OF_SERVICE", "1.0", true)
        );

        assertThatThrownBy(() -> service.validateRequired(agreements))
                .isInstanceOfSatisfying(BaseException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(AuthErrorCode.REQUIRED_AGREEMENT_MISSING));
    }

    @Test
    void rejectsRequiredAgreementThatWasNotAccepted() {
        List<AgreementRequest> agreements = List.of(
                new AgreementRequest("TERMS_OF_SERVICE", "1.0", true),
                new AgreementRequest("PRIVACY_POLICY", "1.0", false)
        );

        assertThatThrownBy(() -> service.validateRequired(agreements))
                .isInstanceOfSatisfying(BaseException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(AuthErrorCode.REQUIRED_AGREEMENT_NOT_ACCEPTED));
    }

    @Test
    void acceptsAllRequiredAgreements() {
        List<AgreementRequest> agreements = List.of(
                new AgreementRequest("TERMS_OF_SERVICE", "1.0", true),
                new AgreementRequest("PRIVACY_POLICY", "1.0", true)
        );

        service.validateRequired(agreements);

        assertThat(agreements).hasSize(2);
    }
}

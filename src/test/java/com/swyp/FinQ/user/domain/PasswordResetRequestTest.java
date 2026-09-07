package com.swyp.FinQ.user.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordResetRequestTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 9, 7, 12, 0);

    @Test
    void evaluatesVerificationExpirationAndResendBoundary() {
        PasswordResetRequest request = createRequest();

        assertThat(request.isVerificationCodeExpired(NOW.plusMinutes(4))).isFalse();
        assertThat(request.isVerificationCodeExpired(NOW.plusMinutes(5))).isTrue();
        assertThat(request.isResendAvailable(NOW.plusSeconds(59))).isFalse();
        assertThat(request.isResendAvailable(NOW.plusMinutes(1))).isTrue();
    }

    @Test
    void countsFailedAttemptsAgainstProvidedLimit() {
        PasswordResetRequest request = createRequest();

        request.recordFailedAttempt();
        request.recordFailedAttempt();

        assertThat(request.getFailedAttemptCount()).isEqualTo(2);
        assertThat(request.hasReachedFailedAttemptLimit(3)).isFalse();

        request.recordFailedAttempt();

        assertThat(request.hasReachedFailedAttemptLimit(3)).isTrue();
    }

    @Test
    void issuesAndConsumesOneTimeResetToken() {
        PasswordResetRequest request = createRequest();

        request.issueResetToken("token-hash", NOW, NOW.plusMinutes(10));

        assertThat(request.isResetTokenAvailable(NOW.plusMinutes(9))).isTrue();
        assertThat(request.isResetTokenAvailable(NOW.plusMinutes(10))).isFalse();

        request.consumeResetToken(NOW.plusMinutes(1));

        assertThat(request.isResetTokenAvailable(NOW.plusMinutes(1))).isFalse();
    }

    @Test
    void resetsVerificationStateWhenCodeIsReissued() {
        PasswordResetRequest request = createRequest();
        request.recordFailedAttempt();
        request.issueResetToken("old-token-hash", NOW, NOW.plusMinutes(10));
        request.consumeResetToken(NOW.plusMinutes(1));

        request.renewVerification(
                "new-code-hash",
                NOW.plusMinutes(6),
                NOW.plusMinutes(2)
        );

        assertThat(request.getVerificationCodeHash()).isEqualTo("new-code-hash");
        assertThat(request.getFailedAttemptCount()).isZero();
        assertThat(request.getVerifiedAt()).isNull();
        assertThat(request.getPasswordResetTokenHash()).isNull();
        assertThat(request.getTokenExpiresAt()).isNull();
        assertThat(request.getConsumedAt()).isNull();
    }

    private PasswordResetRequest createRequest() {
        return PasswordResetRequest.builder()
                .verificationId("verification-id")
                .verificationCodeHash("code-hash")
                .codeExpiresAt(NOW.plusMinutes(5))
                .resendAvailableAt(NOW.plusMinutes(1))
                .build();
    }
}

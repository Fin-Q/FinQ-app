package com.swyp.FinQ.user.service;

import com.swyp.FinQ.global.exception.BaseException;
import com.swyp.FinQ.user.config.PasswordResetProperties;
import com.swyp.FinQ.user.domain.PasswordResetRequest;
import com.swyp.FinQ.user.dto.req.VerificationCodeConfirmRequest;
import com.swyp.FinQ.user.dto.res.VerificationCodeConfirmResponse;
import com.swyp.FinQ.user.exception.AuthErrorCode;
import com.swyp.FinQ.user.repository.PasswordResetRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class VerificationCodeConfirmService {
    private final PasswordResetRequestRepository repository;
    private final VerificationCodeEncoder codeEncoder;
    private final PasswordResetSecretGenerator secretGenerator;
    private final TokenHashEncoder tokenHashEncoder;
    private final PasswordResetProperties properties;
    private final Clock clock;

    // A code mismatch must commit its failure count; other failures still roll back.
    @Transactional(noRollbackFor = CodeMismatchException.class)
    public VerificationCodeConfirmResponse confirm(VerificationCodeConfirmRequest request) {
        PasswordResetRequest verification = repository.findForUpdateByVerificationId(request.verificationId())
                .orElseThrow(() -> BaseException.of(AuthErrorCode.INVALID_PASSWORD_RESET_REQUEST));
        LocalDateTime now = LocalDateTime.ofInstant(clock.instant(), ZoneId.of("Asia/Seoul"));
        if (verification.isVerificationCodeExpired(now)
                || verification.hasReachedFailedAttemptLimit(properties.maximumFailedAttempts())
                || verification.getVerifiedAt() != null
                || verification.getConsumedAt() != null
                || verification.getUser().getPassword() == null) {
            throw BaseException.of(AuthErrorCode.INVALID_PASSWORD_RESET_REQUEST);
        }
        if (!codeEncoder.matches(request.verificationCode(), verification.getVerificationCodeHash())) {
            verification.recordFailedAttempt();
            throw new CodeMismatchException();
        }

        String resetToken = secretGenerator.generateResetToken();
        verification.issueResetToken(tokenHashEncoder.encode(resetToken), now,
                now.plus(properties.tokenExpiration()));
        return new VerificationCodeConfirmResponse(resetToken, properties.tokenExpiration().toSeconds());
    }

    private static final class CodeMismatchException extends BaseException {
        private CodeMismatchException() {
            super(AuthErrorCode.INVALID_PASSWORD_RESET_REQUEST);
        }
    }
}

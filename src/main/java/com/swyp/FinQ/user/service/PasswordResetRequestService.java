package com.swyp.FinQ.user.service;

import com.swyp.FinQ.global.exception.BaseException;
import com.swyp.FinQ.user.config.PasswordResetProperties;
import com.swyp.FinQ.user.domain.PasswordResetRequest;
import com.swyp.FinQ.user.domain.User;
import com.swyp.FinQ.user.dto.res.PasswordResetRequestResponse;
import com.swyp.FinQ.user.exception.AuthErrorCode;
import com.swyp.FinQ.user.repository.PasswordResetRequestRepository;
import com.swyp.FinQ.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetRequestService {

    private static final ZoneId SERVICE_ZONE_ID = ZoneId.of("Asia/Seoul");

    private final UserRepository userRepository;
    private final PasswordResetRequestRepository passwordResetRequestRepository;
    private final PasswordResetSecretGenerator secretGenerator;
    private final VerificationCodeEncoder verificationCodeEncoder;
    private final PasswordResetMailSender mailSender;
    private final PasswordResetProperties properties;
    private final Clock clock;

    @Transactional
    public PasswordResetRequestResponse request(String email) {
        String verificationId = UUID.randomUUID().toString();
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return response(verificationId);
        }

        LocalDateTime now = LocalDateTime.ofInstant(clock.instant(), SERVICE_ZONE_ID);
        passwordResetRequestRepository.findTopByUserIdOrderByCreatedAtDesc(user.getId())
                .filter(request -> !request.isResendAvailable(now))
                .ifPresent(request -> {
                    throw BaseException.of(AuthErrorCode.PASSWORD_RESET_RESEND_TOO_EARLY);
                });

        String verificationCode = secretGenerator.generateVerificationCode();
        String verificationCodeHash = verificationCodeEncoder.encode(verificationCode);
        passwordResetRequestRepository.deleteAllByUserId(user.getId());
        passwordResetRequestRepository.saveAndFlush(PasswordResetRequest.builder()
                .user(user)
                .verificationId(verificationId)
                .verificationCodeHash(verificationCodeHash)
                .codeExpiresAt(now.plus(properties.codeExpiration()))
                .resendAvailableAt(now.plus(properties.resendCooldown()))
                .build());

        mailSender.sendVerificationCode(email, verificationCode, properties.codeExpiration());
        return response(verificationId);
    }

    private PasswordResetRequestResponse response(String verificationId) {
        return new PasswordResetRequestResponse(
                verificationId,
                properties.codeExpiration().toSeconds(),
                properties.resendCooldown().toSeconds()
        );
    }
}

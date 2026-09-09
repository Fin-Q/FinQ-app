package com.swyp.FinQ.user.service;

import com.swyp.FinQ.global.exception.BaseException;
import com.swyp.FinQ.notification.repository.PushTokenRepository;
import com.swyp.FinQ.user.domain.PasswordResetRequest;
import com.swyp.FinQ.user.domain.User;
import com.swyp.FinQ.user.dto.req.PasswordResetConfirmRequest;
import com.swyp.FinQ.user.exception.AuthErrorCode;
import com.swyp.FinQ.user.repository.PasswordResetRequestRepository;
import com.swyp.FinQ.user.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final PasswordResetRequestRepository resetRequestRepository;
    private final TokenHashEncoder tokenHashEncoder;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PushTokenRepository pushTokenRepository;
    private final Clock clock;

    @Transactional
    public void reset(PasswordResetConfirmRequest request) {
        PasswordResetRequest resetRequest = resetRequestRepository
                .findForUpdateByPasswordResetTokenHash(tokenHashEncoder.encode(request.passwordResetToken()))
                .orElseThrow(() -> BaseException.of(AuthErrorCode.INVALID_PASSWORD_RESET_REQUEST));
        LocalDateTime now = LocalDateTime.ofInstant(clock.instant(), ZoneId.of("Asia/Seoul"));
        User user = resetRequest.getUser();
        if (resetRequest.getVerifiedAt() == null || !resetRequest.isResetTokenAvailable(now)
                || user.getPassword() == null) {
            throw BaseException.of(AuthErrorCode.INVALID_PASSWORD_RESET_REQUEST);
        }

        user.changePassword(passwordEncoder.encode(request.newPassword()));
        resetRequest.consumeResetToken(now);
        pushTokenRepository.deleteAllByUser_Id(user.getId());
        refreshTokenRepository.deleteAllByUserId(user.getId());
    }
}

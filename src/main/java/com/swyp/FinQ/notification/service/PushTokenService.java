package com.swyp.FinQ.notification.service;

import com.swyp.FinQ.global.exception.BaseException;
import com.swyp.FinQ.notification.domain.PushToken;
import com.swyp.FinQ.notification.dto.req.PushTokenRegistrationRequest;
import com.swyp.FinQ.notification.dto.res.PushTokenRegistrationResponse;
import com.swyp.FinQ.notification.repository.PushTokenRepository;
import com.swyp.FinQ.user.domain.User;
import com.swyp.FinQ.user.exception.UserErrorCode;
import com.swyp.FinQ.user.repository.UserRepository;
import com.swyp.FinQ.user.service.TokenHashEncoder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PushTokenService {

    private final PushTokenRepository pushTokenRepository;
    private final UserRepository userRepository;
    private final TokenHashEncoder tokenHashEncoder;

    @Transactional
    public PushTokenRegistrationResponse register(
            Long userId,
            String sessionId,
            String deviceId,
            PushTokenRegistrationRequest request
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> BaseException.of(UserErrorCode.USER_NOT_FOUND));
        String fcmTokenHash = tokenHashEncoder.encode(request.fcmToken());

        pushTokenRepository.findByFcmTokenHash(fcmTokenHash)
                .filter(pushToken -> !pushToken.getDeviceId().equals(deviceId))
                .ifPresent(pushTokenRepository::delete);
        pushTokenRepository.flush();

        PushToken pushToken = pushTokenRepository.findByDeviceId(deviceId)
                .map(existing -> {
                    existing.updateRegistration(
                            user,
                            sessionId,
                            request.fcmToken(),
                            fcmTokenHash,
                            request.platform()
                    );
                    return existing;
                })
                .orElseGet(() -> PushToken.builder()
                        .user(user)
                        .sessionId(sessionId)
                        .deviceId(deviceId)
                        .fcmToken(request.fcmToken())
                        .fcmTokenHash(fcmTokenHash)
                        .platform(request.platform())
                        .build());

        return PushTokenRegistrationResponse.from(pushTokenRepository.saveAndFlush(pushToken));
    }
}

package com.swyp.FinQ.notification.service;

import com.swyp.FinQ.notification.repository.PushTokenRepository;
import com.swyp.FinQ.user.service.TokenHashEncoder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PushTokenCleanupService {

    private final PushTokenRepository pushTokenRepository;
    private final TokenHashEncoder tokenHashEncoder;

    @Transactional
    public boolean removeInvalidToken(String fcmToken) {
        String fcmTokenHash = tokenHashEncoder.encode(fcmToken);
        return pushTokenRepository.deleteByFcmTokenHash(fcmTokenHash) > 0;
    }
}

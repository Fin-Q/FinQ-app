package com.swyp.FinQ.user.service;

import com.swyp.FinQ.notification.repository.PushTokenRepository;
import com.swyp.FinQ.user.domain.RefreshToken;
import com.swyp.FinQ.user.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class ExpiredSessionCleanupService {

    private static final ZoneId SERVICE_ZONE_ID = ZoneId.of("Asia/Seoul");

    private final RefreshTokenRepository refreshTokenRepository;
    private final PushTokenRepository pushTokenRepository;
    private final Clock clock;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean cleanupIfExpired(String tokenHash) {
        RefreshToken storedToken = refreshTokenRepository.findByTokenHash(tokenHash).orElse(null);
        LocalDateTime now = LocalDateTime.ofInstant(clock.instant(), SERVICE_ZONE_ID);
        if (storedToken == null || storedToken.getExpiresAt().isAfter(now)) {
            return false;
        }

        pushTokenRepository.deleteByUser_IdAndSessionId(
                storedToken.getUser().getId(), storedToken.getSessionId()
        );
        refreshTokenRepository.delete(storedToken);
        return true;
    }
}

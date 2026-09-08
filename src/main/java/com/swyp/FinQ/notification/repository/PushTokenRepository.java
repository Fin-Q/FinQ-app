package com.swyp.FinQ.notification.repository;

import com.swyp.FinQ.notification.domain.PushToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PushTokenRepository extends JpaRepository<PushToken, Long> {

    Optional<PushToken> findByDeviceId(String deviceId);

    Optional<PushToken> findByFcmTokenHash(String fcmTokenHash);

    Optional<PushToken> findByUser_IdAndDeviceId(Long userId, String deviceId);

    List<PushToken> findAllByUser_IdAndUser_NotificationEnabledTrue(Long userId);

    boolean existsByFcmTokenHash(String fcmTokenHash);

    long deleteByUser_IdAndSessionId(Long userId, String sessionId);

    long deleteByFcmTokenHash(String fcmTokenHash);
}

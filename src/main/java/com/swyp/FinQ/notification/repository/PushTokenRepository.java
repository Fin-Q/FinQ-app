package com.swyp.FinQ.notification.repository;

import com.swyp.FinQ.notification.domain.PushToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PushTokenRepository extends JpaRepository<PushToken, Long> {

    Optional<PushToken> findByDeviceId(String deviceId);

    Optional<PushToken> findByFcmTokenHash(String fcmTokenHash);
}

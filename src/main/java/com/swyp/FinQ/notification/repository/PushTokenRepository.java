package com.swyp.FinQ.notification.repository;

import com.swyp.FinQ.notification.domain.PushToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PushTokenRepository extends JpaRepository<PushToken, Long> {

    Optional<PushToken> findByDeviceId(String deviceId);

    Optional<PushToken> findByFcmTokenHash(String fcmTokenHash);

    Optional<PushToken> findByUser_IdAndDeviceId(Long userId, String deviceId);

    List<PushToken> findAllByUser_IdAndUser_NotificationEnabledTrueAndActiveTrue(Long userId);

    List<PushToken> findByUser_NotificationEnabledTrueAndActiveTrueAndIdGreaterThanOrderByIdAsc(
            Long afterId, Pageable pageable);

    boolean existsByFcmTokenHash(String fcmTokenHash);

    long deleteByUser_IdAndSessionId(Long userId, String sessionId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update PushToken p set p.active = false where p.user.id = :userId and p.sessionId = :sessionId and p.active = true")
    int deactivateByUserIdAndSessionId(@Param("userId") Long userId, @Param("sessionId") String sessionId);

    long deleteByFcmTokenHash(String fcmTokenHash);

    void deleteAllByUser_Id(Long userId);
}

package com.swyp.FinQ.notification.domain;

import com.swyp.FinQ.global.entity.BaseTimeEntity;
import com.swyp.FinQ.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "push_token",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_push_token_device_id", columnNames = "device_id"),
                @UniqueConstraint(name = "uk_push_token_fcm_token_hash", columnNames = "fcm_token_hash")
        },
        indexes = {
                @Index(name = "idx_push_token_user", columnList = "user_id"),
                @Index(name = "idx_push_token_user_session", columnList = "user_id, session_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class PushToken extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "push_token_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "session_id", nullable = false, length = 36)
    private String sessionId;

    @Column(name = "device_id", nullable = false, length = 255)
    private String deviceId;

    @Column(name = "fcm_token", nullable = false, length = 2048)
    private String fcmToken;

    @Column(name = "fcm_token_hash", nullable = false, length = 64)
    private String fcmTokenHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "platform", nullable = false, length = 20)
    private PushPlatform platform;

    public void updateRegistration(
            User user,
            String sessionId,
            String fcmToken,
            String fcmTokenHash,
            PushPlatform platform
    ) {
        this.user = user;
        this.sessionId = sessionId;
        this.fcmToken = fcmToken;
        this.fcmTokenHash = fcmTokenHash;
        this.platform = platform;
    }
}

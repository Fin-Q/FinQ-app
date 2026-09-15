package com.swyp.FinQ.notification.repository;

import com.swyp.FinQ.global.config.JpaAuditingConfig;
import com.swyp.FinQ.notification.domain.PushPlatform;
import com.swyp.FinQ.notification.domain.PushToken;
import com.swyp.FinQ.support.MySqlContainerSupport;
import com.swyp.FinQ.user.domain.ProfileImageCode;
import com.swyp.FinQ.user.domain.User;
import com.swyp.FinQ.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(JpaAuditingConfig.class)
class PushTokenRepositoryTest extends MySqlContainerSupport {

    @Autowired
    private PushTokenRepository pushTokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("알림 동의 사용자만 ID 커서로 조회하며 앞 배치 삭제 후에도 다음 토큰을 누락하지 않는다")
    void findsEnabledTokensByCursorWithoutOffset() {
        User enabled = createUser();
        User disabled = createUser();
        disabled.updateNotificationEnabled(false);
        PushToken first = pushTokenRepository.saveAndFlush(createPushToken(enabled, "first", "first-token"));
        pushTokenRepository.saveAndFlush(createPushToken(disabled, "disabled", "disabled-token"));
        PushToken last = pushTokenRepository.saveAndFlush(createPushToken(enabled, "last", "last-token"));
        entityManager.flush();
        entityManager.clear();

        assertThat(pushTokenRepository.findByUser_NotificationEnabledTrueAndActiveTrueAndIdGreaterThanOrderByIdAsc(
                0L, PageRequest.of(0, 1))).extracting(PushToken::getId).containsExactly(first.getId());

        pushTokenRepository.deleteById(first.getId());
        pushTokenRepository.flush();
        entityManager.clear();
        assertThat(pushTokenRepository.findByUser_NotificationEnabledTrueAndActiveTrueAndIdGreaterThanOrderByIdAsc(
                first.getId(), PageRequest.of(0, 1))).extracting(PushToken::getId).containsExactly(last.getId());
        assertThat(pushTokenRepository.findByUser_NotificationEnabledTrueAndActiveTrueAndIdGreaterThanOrderByIdAsc(
                last.getId(), PageRequest.of(0, 1))).isEmpty();
    }

    @Test
    @DisplayName("기기 ID와 FCM 토큰으로 푸시 토큰을 조회한다")
    void findPushToken() {
        PushToken saved = pushTokenRepository.saveAndFlush(createPushToken(createUser(), "device-1", "token-1"));

        assertThat(pushTokenRepository.findByDeviceId("device-1")).contains(saved);
        assertThat(pushTokenRepository.findByFcmTokenHash(hashOf("token-1"))).contains(saved);
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void preservesInactiveTokenRegistration() {
        PushToken saved = pushTokenRepository.saveAndFlush(createPushToken(createUser(), "inactive-device", "inactive-token"));
        assertThat(saved.isActive()).isTrue();

        saved.deactivate();
        entityManager.flush();
        entityManager.clear();

        PushToken inactive = pushTokenRepository.findByDeviceId("inactive-device").orElseThrow();
        assertThat(inactive.isActive()).isFalse();
        assertThat(inactive.getId()).isEqualTo(saved.getId());
        assertThat(inactive.getFcmToken()).isEqualTo("inactive-token");
    }

    @Test
    void excludesInactiveTokensFromBothDeliveryQueries() {
        User user = createUser();
        PushToken active = pushTokenRepository.saveAndFlush(createPushToken(user, "active-device", "active-token"));
        PushToken inactive = pushTokenRepository.saveAndFlush(createPushToken(user, "inactive-device", "inactive-token"));
        inactive.deactivate();
        entityManager.flush();
        entityManager.clear();

        assertThat(pushTokenRepository.findAllByUser_IdAndUser_NotificationEnabledTrueAndActiveTrue(user.getId()))
                .extracting(PushToken::getId).containsExactly(active.getId());
        assertThat(pushTokenRepository.findByUser_NotificationEnabledTrueAndActiveTrueAndIdGreaterThanOrderByIdAsc(
                0L, PageRequest.of(0, 500)))
                .extracting(PushToken::getId).containsExactly(active.getId());
        assertThat(pushTokenRepository.findByDeviceId("inactive-device")).isPresent();
    }

    @Test
    void deactivatesOnlyMatchingUserAndSessionAndIsIdempotent() {
        User user = createUser();
        User otherUser = createUser();
        PushToken target = pushTokenRepository.saveAndFlush(createPushToken(user, "target-device", "target-token"));
        PushToken other = pushTokenRepository.saveAndFlush(createPushToken(otherUser, "other-device", "other-token"));

        assertThat(pushTokenRepository.deactivateByUserIdAndSessionId(user.getId(), "wrong-session")).isZero();
        assertThat(pushTokenRepository.deactivateByUserIdAndSessionId(user.getId(), target.getSessionId())).isEqualTo(1);
        assertThat(pushTokenRepository.deactivateByUserIdAndSessionId(user.getId(), target.getSessionId())).isZero();
        assertThat(pushTokenRepository.findById(target.getId()).orElseThrow().isActive()).isFalse();
        assertThat(pushTokenRepository.findById(other.getId()).orElseThrow().isActive()).isTrue();
    }

    @Test
    @DisplayName("동일한 기기 ID를 중복 저장할 수 없다")
    void rejectDuplicateDeviceId() {
        User user = createUser();
        pushTokenRepository.saveAndFlush(createPushToken(user, "device-1", "token-1"));

        assertThatThrownBy(() ->
                pushTokenRepository.saveAndFlush(createPushToken(user, "device-1", "token-2"))
        ).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("동일한 FCM 토큰을 중복 저장할 수 없다")
    void rejectDuplicateFcmToken() {
        User user = createUser();
        pushTokenRepository.saveAndFlush(createPushToken(user, "device-1", "token-1"));

        assertThatThrownBy(() ->
                pushTokenRepository.saveAndFlush(createPushToken(user, "device-2", "token-1"))
        ).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("회원 삭제 시 푸시 토큰도 연쇄 삭제된다")
    void cascadeDeleteWithUser() {
        User user = createUser();
        pushTokenRepository.saveAndFlush(createPushToken(user, "device-1", "token-1"));
        entityManager.clear();

        userRepository.deleteById(user.getId());
        userRepository.flush();
        entityManager.clear();

        assertThat(pushTokenRepository.findByDeviceId("device-1")).isEmpty();
    }

    private User createUser() {
        return userRepository.saveAndFlush(User.builder()
                .email("user-" + System.nanoTime() + "@example.com")
                .password("encoded-password")
                .nickname("Minter")
                .profileImageCode(ProfileImageCode.PROFILE_01)
                .build());
    }

    private PushToken createPushToken(User user, String deviceId, String fcmToken) {
        return PushToken.builder()
                .user(user)
                .sessionId("session-id")
                .deviceId(deviceId)
                .fcmToken(fcmToken)
                .fcmTokenHash(hashOf(fcmToken))
                .platform(PushPlatform.IOS)
                .build();
    }

    private String hashOf(String token) {
        return "%064x".formatted(token.hashCode());
    }
}

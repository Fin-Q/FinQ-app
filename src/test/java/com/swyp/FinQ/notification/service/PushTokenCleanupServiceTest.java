package com.swyp.FinQ.notification.service;

import com.swyp.FinQ.notification.domain.PushPlatform;
import com.swyp.FinQ.notification.domain.PushToken;
import com.swyp.FinQ.notification.repository.PushTokenRepository;
import com.swyp.FinQ.support.MySqlContainerSupport;
import com.swyp.FinQ.user.domain.ProfileImageCode;
import com.swyp.FinQ.user.domain.User;
import com.swyp.FinQ.user.repository.UserRepository;
import com.swyp.FinQ.user.service.TokenHashEncoder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class PushTokenCleanupServiceTest extends MySqlContainerSupport {

    @Autowired
    private PushTokenCleanupService pushTokenCleanupService;

    @Autowired
    private PushTokenRepository pushTokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenHashEncoder tokenHashEncoder;

    @Test
    void removesInvalidTokenByHash() {
        savePushToken("invalid-fcm-token");

        boolean removed = pushTokenCleanupService.removeInvalidToken("invalid-fcm-token");

        assertThat(removed).isTrue();
        assertThat(pushTokenRepository.findAll()).isEmpty();
    }

    @Test
    void ignoresAlreadyRemovedToken() {
        savePushToken("invalid-fcm-token");
        pushTokenCleanupService.removeInvalidToken("invalid-fcm-token");

        boolean removedAgain = pushTokenCleanupService.removeInvalidToken("invalid-fcm-token");

        assertThat(removedAgain).isFalse();
        assertThat(pushTokenRepository.findAll()).isEmpty();
    }

    private void savePushToken(String fcmToken) {
        User user = userRepository.save(User.builder()
                .email("cleanup@example.com")
                .password("encoded-password")
                .nickname("Minter")
                .profileImageCode(ProfileImageCode.PROFILE_01)
                .build());
        pushTokenRepository.saveAndFlush(PushToken.builder()
                .user(user)
                .sessionId("session-id")
                .deviceId("device-1")
                .fcmToken(fcmToken)
                .fcmTokenHash(tokenHashEncoder.encode(fcmToken))
                .platform(PushPlatform.IOS)
                .build());
    }
}

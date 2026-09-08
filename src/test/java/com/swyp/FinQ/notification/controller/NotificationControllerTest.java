package com.swyp.FinQ.notification.controller;

import com.swyp.FinQ.global.security.token.IssuedTokenPair;
import com.swyp.FinQ.global.security.token.JwtTokenProvider;
import com.swyp.FinQ.notification.repository.PushTokenRepository;
import com.swyp.FinQ.support.MySqlContainerSupport;
import com.swyp.FinQ.user.domain.ProfileImageCode;
import com.swyp.FinQ.user.domain.User;
import com.swyp.FinQ.user.repository.UserRepository;
import com.swyp.FinQ.user.service.TokenHashEncoder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class NotificationControllerTest extends MySqlContainerSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PushTokenRepository pushTokenRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private TokenHashEncoder tokenHashEncoder;

    @Test
    void registersAndUpdatesPushTokenForDevice() throws Exception {
        User user = saveUser("push@example.com");
        String bearerToken = bearerToken(user.getId());

        register(bearerToken, "device-1", "fcm-token-1")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("푸시 토큰이 저장되었습니다."))
                .andExpect(jsonPath("$.data.deviceId").value("device-1"))
                .andExpect(jsonPath("$.data.platform").value("IOS"))
                .andExpect(jsonPath("$.data.updatedAt").exists());

        register(bearerToken, "device-1", "fcm-token-2")
                .andExpect(status().isOk());

        assertThat(pushTokenRepository.findAll()).hasSize(1);
        assertThat(pushTokenRepository.findByDeviceId("device-1").orElseThrow().getFcmToken())
                .isEqualTo("fcm-token-2");
    }

    @Test
    void movesSameFcmTokenToLatestDevice() throws Exception {
        User firstUser = saveUser("first@example.com");
        User secondUser = saveUser("second@example.com");

        register(bearerToken(firstUser.getId()), "device-1", "shared-token")
                .andExpect(status().isOk());
        register(bearerToken(secondUser.getId()), "device-2", "shared-token")
                .andExpect(status().isOk());

        assertThat(pushTokenRepository.findAll()).hasSize(1);
        assertThat(pushTokenRepository.findByDeviceId("device-1")).isEmpty();
        assertThat(pushTokenRepository.findByDeviceId("device-2").orElseThrow().getUser().getId())
                .isEqualTo(secondUser.getId());
        assertThat(pushTokenRepository.existsByFcmTokenHash(tokenHashEncoder.encode("shared-token")))
                .isTrue();
    }

    @Test
    void rejectsInvalidPushTokenRequest() throws Exception {
        User user = saveUser("invalid@example.com");

        mockMvc.perform(post("/users/me/push-tokens/device-1")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(user.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fcmToken": " ",
                                  "platform": "ANDROID"
                                }
                                """))
                .andExpect(status().isBadRequest());

        assertThat(pushTokenRepository.findAll()).isEmpty();
    }

    @Test
    void rejectsPushTokenRegistrationWithoutAccessToken() throws Exception {
        mockMvc.perform(post("/users/me/push-tokens/device-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fcmToken": "fcm-token",
                                  "platform": "IOS"
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updatesNotificationSetting() throws Exception {
        User user = saveUser("setting@example.com");

        mockMvc.perform(patch("/users/me/notification-settings")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(user.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "notificationEnabled": false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("알림 설정이 변경되었습니다."))
                .andExpect(jsonPath("$.data.notificationEnabled").value(false))
                .andExpect(jsonPath("$.data.updatedAt").exists());

        assertThat(userRepository.findById(user.getId()).orElseThrow().isNotificationEnabled())
                .isFalse();
    }

    @Test
    void rejectsMissingNotificationSetting() throws Exception {
        User user = saveUser("missing-setting@example.com");

        mockMvc.perform(patch("/users/me/notification-settings")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(user.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("COMMON_VALIDATION_ERROR"));

        assertThat(userRepository.findById(user.getId()).orElseThrow().isNotificationEnabled())
                .isTrue();
    }

    @Test
    void rejectsNotificationSettingWithoutAccessToken() throws Exception {
        mockMvc.perform(patch("/users/me/notification-settings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "notificationEnabled": false
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unregistersOwnedPushToken() throws Exception {
        User user = saveUser("unregister@example.com");
        String bearerToken = bearerToken(user.getId());
        register(bearerToken, "device-1", "fcm-token")
                .andExpect(status().isOk());

        mockMvc.perform(delete("/users/me/push-tokens/device-1")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("푸시 토큰 등록이 해제되었습니다."))
                .andExpect(jsonPath("$.data.deviceId").value("device-1"))
                .andExpect(jsonPath("$.data.unregisteredAt").exists());

        assertThat(pushTokenRepository.findByDeviceId("device-1")).isEmpty();
    }

    @Test
    void doesNotUnregisterAnotherUsersPushToken() throws Exception {
        User owner = saveUser("owner@example.com");
        User requester = saveUser("requester@example.com");
        register(bearerToken(owner.getId()), "device-1", "fcm-token")
                .andExpect(status().isOk());

        mockMvc.perform(delete("/users/me/push-tokens/device-1")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(requester.getId())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("PUSH_TOKEN_NOT_FOUND"));

        assertThat(pushTokenRepository.findByDeviceId("device-1")).isPresent();
    }

    private org.springframework.test.web.servlet.ResultActions register(
            String bearerToken,
            String deviceId,
            String fcmToken
    ) throws Exception {
        return mockMvc.perform(post("/users/me/push-tokens/{deviceId}", deviceId)
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "fcmToken": "%s",
                          "platform": "IOS"
                        }
                        """.formatted(fcmToken)));
    }

    private User saveUser(String email) {
        return userRepository.saveAndFlush(User.builder()
                .email(email)
                .password("encoded-password")
                .nickname("Minter")
                .profileImageCode(ProfileImageCode.PROFILE_01)
                .build());
    }

    private String bearerToken(Long userId) {
        IssuedTokenPair tokens = jwtTokenProvider.issue(userId);
        return "Bearer " + tokens.accessToken();
    }
}

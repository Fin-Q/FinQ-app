package com.swyp.FinQ.notification.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FirebaseConfigTest {

    private final FirebaseConfig firebaseConfig = new FirebaseConfig();

    @Test
    @DisplayName("Firebase가 활성화됐지만 인증정보가 비어 있으면 초기화를 거부한다")
    void rejectsEmptyCredentials() {
        assertThatThrownBy(() -> firebaseConfig.loadCredentials(" "))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Firebase credentials must be configured when Firebase is enabled");
    }

    @Test
    @DisplayName("Base64 형식이 아닌 Firebase 인증정보를 거부한다")
    void rejectsInvalidBase64Credentials() {
        assertThatThrownBy(() -> firebaseConfig.loadCredentials("not-base64"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Firebase credentials are invalid");
    }

    @Test
    @DisplayName("유효한 JSON이 아닌 Firebase 인증정보를 거부한다")
    void rejectsInvalidCredentialJson() {
        String encoded = Base64.getEncoder().encodeToString(
                "{}".getBytes(StandardCharsets.UTF_8)
        );

        assertThatThrownBy(() -> firebaseConfig.loadCredentials(encoded))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Firebase credentials are invalid");
    }
}

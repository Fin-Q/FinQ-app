package com.swyp.FinQ.user.service;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OAuthTokenCipherTest {

    private static final String VALID_KEY = Base64.getEncoder()
            .encodeToString("0123456789abcdef0123456789abcdef".getBytes(StandardCharsets.UTF_8));

    private final OAuthTokenCipher cipher = new OAuthTokenCipher(VALID_KEY, new SecureRandom());

    @Test
    void encryptsAndDecryptsToken() {
        String encryptedToken = cipher.encrypt("apple-refresh-token");

        assertThat(encryptedToken).startsWith("v1.");
        assertThat(cipher.decrypt(encryptedToken)).isEqualTo("apple-refresh-token");
    }

    @Test
    void usesDifferentIvForEachEncryption() {
        String first = cipher.encrypt("same-token");
        String second = cipher.encrypt("same-token");

        assertThat(first).isNotEqualTo(second);
        assertThat(cipher.decrypt(first)).isEqualTo("same-token");
        assertThat(cipher.decrypt(second)).isEqualTo("same-token");
    }

    @Test
    void rejectsTamperedCiphertext() {
        String encryptedToken = cipher.encrypt("apple-refresh-token");
        String[] parts = encryptedToken.split("\\.");
        byte[] ciphertext = Base64.getUrlDecoder().decode(parts[2]);
        ciphertext[ciphertext.length - 1] ^= 1;
        String tamperedToken = String.join(
                ".",
                parts[0],
                parts[1],
                Base64.getUrlEncoder().withoutPadding().encodeToString(ciphertext)
        );

        assertThatThrownBy(() -> cipher.decrypt(tamperedToken))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("OAuth token ciphertext authentication failed");
    }

    @Test
    void rejectsInvalidEncryptionKey() {
        OAuthTokenCipher invalidCipher = new OAuthTokenCipher(
                Base64.getEncoder().encodeToString("too-short".getBytes(StandardCharsets.UTF_8)),
                new SecureRandom()
        );

        assertThatThrownBy(() -> invalidCipher.encrypt("apple-refresh-token"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("OAuth token encryption key must decode to exactly 32 bytes");
    }
}

package com.swyp.FinQ.user.service;

import com.swyp.FinQ.user.config.SocialOAuthProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.AEADBadTagException;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class OAuthTokenCipher {

    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final String KEY_ALGORITHM = "AES";
    private static final String VERSION = "v1";
    private static final int KEY_LENGTH_BYTES = 32;
    private static final int IV_LENGTH_BYTES = 12;
    private static final int AUTHENTICATION_TAG_LENGTH_BITS = 128;

    private final String encodedKey;
    private final SecureRandom secureRandom;

    @Autowired
    public OAuthTokenCipher(SocialOAuthProperties properties) {
        this(properties.tokenEncryptionKey(), new SecureRandom());
    }

    OAuthTokenCipher(String encodedKey, SecureRandom secureRandom) {
        this.encodedKey = encodedKey;
        this.secureRandom = secureRandom;
    }

    public String encrypt(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("OAuth token must not be blank");
        }

        byte[] iv = new byte[IV_LENGTH_BYTES];
        secureRandom.nextBytes(iv);

        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(
                    Cipher.ENCRYPT_MODE,
                    encryptionKey(),
                    new GCMParameterSpec(AUTHENTICATION_TAG_LENGTH_BITS, iv)
            );
            cipher.updateAAD(VERSION.getBytes(StandardCharsets.UTF_8));
            byte[] encryptedToken = cipher.doFinal(token.getBytes(StandardCharsets.UTF_8));

            Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
            return String.join(".", VERSION, encoder.encodeToString(iv), encoder.encodeToString(encryptedToken));
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("OAuth token encryption failed", exception);
        }
    }

    public String decrypt(String encryptedToken) {
        if (encryptedToken == null || encryptedToken.isBlank()) {
            throw new IllegalArgumentException("Encrypted OAuth token must not be blank");
        }

        String[] parts = encryptedToken.split("\\.", -1);
        if (parts.length != 3 || !VERSION.equals(parts[0])) {
            throw new IllegalArgumentException("Unsupported OAuth token ciphertext format");
        }

        try {
            Base64.Decoder decoder = Base64.getUrlDecoder();
            byte[] iv = decoder.decode(parts[1]);
            if (iv.length != IV_LENGTH_BYTES) {
                throw new IllegalArgumentException("Invalid OAuth token ciphertext IV");
            }

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(
                    Cipher.DECRYPT_MODE,
                    encryptionKey(),
                    new GCMParameterSpec(AUTHENTICATION_TAG_LENGTH_BITS, iv)
            );
            cipher.updateAAD(VERSION.getBytes(StandardCharsets.UTF_8));
            byte[] decryptedToken = cipher.doFinal(decoder.decode(parts[2]));
            return new String(decryptedToken, StandardCharsets.UTF_8);
        } catch (AEADBadTagException exception) {
            throw new IllegalArgumentException("OAuth token ciphertext authentication failed", exception);
        } catch (IllegalArgumentException exception) {
            throw exception;
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("OAuth token decryption failed", exception);
        }
    }

    private SecretKeySpec encryptionKey() {
        if (encodedKey == null || encodedKey.isBlank()) {
            throw new IllegalStateException("OAuth token encryption key is not configured");
        }

        final byte[] key;
        try {
            key = Base64.getDecoder().decode(encodedKey);
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException("OAuth token encryption key is not valid Base64", exception);
        }

        if (key.length != KEY_LENGTH_BYTES) {
            throw new IllegalStateException("OAuth token encryption key must decode to exactly 32 bytes");
        }
        return new SecretKeySpec(key, KEY_ALGORITHM);
    }
}

package com.swyp.FinQ.user.service;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;

@Component
public class PasswordResetSecretGenerator {

    private static final int VERIFICATION_CODE_BOUND = 1_000_000;
    private static final int RESET_TOKEN_BYTE_LENGTH = 32;

    private final SecureRandom secureRandom = new SecureRandom();

    public String generateVerificationCode() {
        return "%06d".formatted(secureRandom.nextInt(VERIFICATION_CODE_BOUND));
    }

    public String generateResetToken() {
        byte[] tokenBytes = new byte[RESET_TOKEN_BYTE_LENGTH];
        secureRandom.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }
}

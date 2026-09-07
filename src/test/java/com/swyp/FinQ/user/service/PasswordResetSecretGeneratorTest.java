package com.swyp.FinQ.user.service;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordResetSecretGeneratorTest {

    private final PasswordResetSecretGenerator secretGenerator = new PasswordResetSecretGenerator();
    private final VerificationCodeEncoder codeEncoder =
            new VerificationCodeEncoder(new BCryptPasswordEncoder());
    private final TokenHashEncoder tokenHashEncoder = new TokenHashEncoder();

    @Test
    void generatesSixDigitVerificationCode() {
        String verificationCode = secretGenerator.generateVerificationCode();

        assertThat(verificationCode).matches("\\d{6}");
    }

    @Test
    void storesVerificationCodeAsBcryptHash() {
        String verificationCode = secretGenerator.generateVerificationCode();
        String encodedCode = codeEncoder.encode(verificationCode);

        assertThat(encodedCode).isNotEqualTo(verificationCode);
        assertThat(codeEncoder.matches(verificationCode, encodedCode)).isTrue();
    }

    @Test
    void generatesUrlSafeResetTokenAndHashesIt() {
        String resetToken = secretGenerator.generateResetToken();
        String tokenHash = tokenHashEncoder.encode(resetToken);

        assertThat(Base64.getUrlDecoder().decode(resetToken)).hasSize(32);
        assertThat(tokenHash).hasSize(64).doesNotContain(resetToken);
    }
}

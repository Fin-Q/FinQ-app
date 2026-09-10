package com.swyp.FinQ.user.service;

import com.nimbusds.jose.crypto.ECDSAVerifier;
import com.nimbusds.jwt.SignedJWT;
import com.swyp.FinQ.user.config.AppleProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AppleClientSecretGeneratorTest {

    private static final Instant NOW = Instant.parse("2026-09-08T00:00:00Z");

    private KeyPair keyPair;

    @BeforeEach
    void setUp() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
        generator.initialize(new ECGenParameterSpec("secp256r1"));
        keyPair = generator.generateKeyPair();
    }

    @Test
    void Apple_규격의_ES256_client_secret을_생성한다() throws Exception {
        AppleProperties properties = properties(
                Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded())
        );
        AppleClientSecretGenerator generator = new AppleClientSecretGenerator(
                properties,
                Clock.fixed(NOW, ZoneOffset.UTC)
        );

        SignedJWT clientSecret = SignedJWT.parse(generator.generate());

        assertThat(clientSecret.verify(new ECDSAVerifier((ECPublicKey) keyPair.getPublic()))).isTrue();
        assertThat(clientSecret.getHeader().getAlgorithm().getName()).isEqualTo("ES256");
        assertThat(clientSecret.getHeader().getKeyID()).isEqualTo("apple-key-id");
        assertThat(clientSecret.getJWTClaimsSet().getIssuer()).isEqualTo("apple-team-id");
        assertThat(clientSecret.getJWTClaimsSet().getSubject()).isEqualTo("com.finq.app");
        assertThat(clientSecret.getJWTClaimsSet().getAudience())
                .containsExactly("https://appleid.apple.com");
        assertThat(clientSecret.getJWTClaimsSet().getIssueTime().toInstant()).isEqualTo(NOW);
        assertThat(clientSecret.getJWTClaimsSet().getExpirationTime().toInstant())
                .isEqualTo(NOW.plus(Duration.ofMinutes(5)));
    }

    @Test
    void Base64로_감싼_PEM_private_key도_읽는다() throws Exception {
        String pem = """
                -----BEGIN PRIVATE KEY-----
                %s
                -----END PRIVATE KEY-----
                """.formatted(Base64.getMimeEncoder().encodeToString(keyPair.getPrivate().getEncoded()));
        AppleClientSecretGenerator generator = new AppleClientSecretGenerator(
                properties(Base64.getEncoder().encodeToString(pem.getBytes())),
                Clock.fixed(NOW, ZoneOffset.UTC)
        );

        SignedJWT clientSecret = SignedJWT.parse(generator.generate());

        assertThat(clientSecret.verify(new ECDSAVerifier((ECPublicKey) keyPair.getPublic()))).isTrue();
    }

    @Test
    void private_key_설정이_올바르지_않으면_실패한다() {
        AppleClientSecretGenerator generator = new AppleClientSecretGenerator(
                properties("not-base64"),
                Clock.fixed(NOW, ZoneOffset.UTC)
        );

        assertThatThrownBy(generator::generate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("APPLE_PRIVATE_KEY_BASE64 형식이 올바르지 않습니다.");
    }

    private AppleProperties properties(String privateKeyBase64) {
        return new AppleProperties(
                "com.finq.app",
                "apple-team-id",
                "apple-key-id",
                privateKeyBase64,
                URI.create("https://appleid.apple.com"),
                URI.create("https://appleid.apple.com/auth/keys"),
                URI.create("https://appleid.apple.com/auth/token"),
                URI.create("https://appleid.apple.com/auth/revoke"),
                Duration.ofMinutes(5)
        );
    }
}

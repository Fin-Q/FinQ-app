package com.swyp.FinQ.user.service;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jose.RemoteKeySourceException;
import com.swyp.FinQ.global.exception.BaseException;
import com.swyp.FinQ.user.config.AppleConfig;
import com.swyp.FinQ.user.config.AppleProperties;
import com.swyp.FinQ.user.exception.AuthErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.net.URI;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class AppleIdentityTokenClientTest {

    private static final String CLIENT_ID = "com.finq.app";
    private static final String ISSUER = "https://appleid.apple.com";
    private static final String RAW_NONCE = "one-time-raw-nonce";
    private static final String SUBJECT = "apple-user-id";

    private AppleNonceHasher nonceHasher;
    private AppleProperties properties;
    private RSAKey signingKey;
    private AppleIdentityTokenClient client;

    @BeforeEach
    void setUp() throws Exception {
        nonceHasher = new AppleNonceHasher();
        properties = new AppleProperties(
                CLIENT_ID,
                "apple-team-id",
                "apple-key-id",
                "unused-private-key",
                URI.create(ISSUER),
                URI.create("https://appleid.apple.com/auth/keys"),
                URI.create("https://appleid.apple.com/auth/token"),
                Duration.ofMinutes(5)
        );
        signingKey = generateRsaKey("apple-test-key");
        client = new AppleIdentityTokenClient(
                decoder(signingKey.toRSAPublicKey()),
                nonceHasher,
                properties
        );
    }

    @Test
    void verifiesValidAppleIdentityToken() {
        String identityToken = identityToken(signingKey, ISSUER, CLIENT_ID, RAW_NONCE, validExpiration());

        AppleUserIdentity identity = client.verify(identityToken, RAW_NONCE);

        assertThat(identity.providerUserId()).isEqualTo(SUBJECT);
    }

    @Test
    void rejectsTokenSignedWithUnknownKey() throws Exception {
        RSAKey unknownKey = generateRsaKey("unknown-key");
        String identityToken = identityToken(unknownKey, ISSUER, CLIENT_ID, RAW_NONCE, validExpiration());

        assertInvalidToken(identityToken, RAW_NONCE);
    }

    @Test
    void rejectsExpiredToken() {
        String identityToken = identityToken(
                signingKey,
                ISSUER,
                CLIENT_ID,
                RAW_NONCE,
                Instant.now().minusSeconds(120)
        );

        assertInvalidToken(identityToken, RAW_NONCE);
    }

    @Test
    void rejectsTokenWithDifferentIssuer() {
        String identityToken = identityToken(
                signingKey,
                "https://attacker.example.com",
                CLIENT_ID,
                RAW_NONCE,
                validExpiration()
        );

        assertInvalidToken(identityToken, RAW_NONCE);
    }

    @Test
    void rejectsTokenIssuedForDifferentClient() {
        String identityToken = identityToken(
                signingKey,
                ISSUER,
                "com.other.app",
                RAW_NONCE,
                validExpiration()
        );

        assertInvalidToken(identityToken, RAW_NONCE);
    }

    @Test
    void rejectsTokenWhenNonceDoesNotMatch() {
        String identityToken = identityToken(signingKey, ISSUER, CLIENT_ID, RAW_NONCE, validExpiration());

        assertInvalidToken(identityToken, "different-raw-nonce");
    }

    @Test
    void rejectsRequestWithoutNonce() {
        String identityToken = identityToken(signingKey, ISSUER, CLIENT_ID, RAW_NONCE, validExpiration());

        assertInvalidToken(identityToken, " ");
    }

    @Test
    void mapsApplePublicKeyServerFailureToServiceUnavailable() {
        JwtDecoder unavailableDecoder = mock(JwtDecoder.class);
        given(unavailableDecoder.decode("identity-token")).willThrow(new JwtException(
                "Apple JWK Set unavailable",
                new RemoteKeySourceException("Apple JWK Set unavailable", new java.io.IOException())
        ));
        AppleIdentityTokenClient unavailableClient = new AppleIdentityTokenClient(
                unavailableDecoder,
                nonceHasher,
                properties
        );

        assertThatThrownBy(() -> unavailableClient.verify("identity-token", RAW_NONCE))
                .isInstanceOfSatisfying(BaseException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(AuthErrorCode.APPLE_AUTH_SERVER_UNAVAILABLE));
    }

    private JwtDecoder decoder(RSAPublicKey publicKey) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey(publicKey)
                .signatureAlgorithm(SignatureAlgorithm.RS256)
                .build();
        decoder.setJwtValidator(new AppleConfig().appleIdentityTokenValidator(properties));
        return decoder;
    }

    private String identityToken(
            RSAKey key,
            String issuer,
            String audience,
            String rawNonce,
            Instant expiresAt
    ) {
        Instant issuedAt = expiresAt.minusSeconds(300);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .subject(SUBJECT)
                .audience(java.util.List.of(audience))
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .claim("nonce", nonceHasher.hash(rawNonce))
                .build();
        JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256)
                .keyId(key.getKeyID())
                .build();
        NimbusJwtEncoder encoder = new NimbusJwtEncoder(
                new ImmutableJWKSet<SecurityContext>(new JWKSet(key))
        );
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    private RSAKey generateRsaKey(String keyId) throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair keyPair = generator.generateKeyPair();
        return new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
                .privateKey((RSAPrivateKey) keyPair.getPrivate())
                .keyID(keyId)
                .build();
    }

    private Instant validExpiration() {
        return Instant.now().plusSeconds(300);
    }

    private void assertInvalidToken(String identityToken, String nonce) {
        assertThatThrownBy(() -> client.verify(identityToken, nonce))
                .isInstanceOfSatisfying(BaseException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(AuthErrorCode.INVALID_APPLE_IDENTITY_TOKEN));
    }
}

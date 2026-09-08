package com.swyp.FinQ.user.service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.swyp.FinQ.user.config.AppleProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

@Component
public class AppleClientSecretGenerator {

    private final AppleProperties properties;
    private final Clock clock;

    @Autowired
    public AppleClientSecretGenerator(AppleProperties properties) {
        this(properties, Clock.systemUTC());
    }

    AppleClientSecretGenerator(AppleProperties properties, Clock clock) {
        this.properties = properties;
        this.clock = clock;
    }

    public String generate() {
        validateConfiguration();

        Instant issuedAt = clock.instant();
        Duration expiration = properties.clientSecretExpiration();
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .issuer(properties.teamId())
                .subject(properties.clientId())
                .audience(properties.issuer().toString())
                .issueTime(Date.from(issuedAt))
                .expirationTime(Date.from(issuedAt.plus(expiration)))
                .build();
        SignedJWT clientSecret = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.ES256)
                        .keyID(properties.keyId())
                        .build(),
                claims
        );

        try {
            clientSecret.sign(new ECDSASigner(parsePrivateKey()));
            return clientSecret.serialize();
        } catch (JOSEException exception) {
            throw new IllegalStateException("Apple client secret 생성에 실패했습니다.", exception);
        }
    }

    private ECPrivateKey parsePrivateKey() {
        try {
            byte[] decoded = Base64.getDecoder().decode(properties.privateKeyBase64());
            byte[] keyBytes = decodePemIfNecessary(decoded);
            return (ECPrivateKey) KeyFactory.getInstance("EC")
                    .generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
        } catch (IllegalArgumentException | ClassCastException exception) {
            throw new IllegalStateException("APPLE_PRIVATE_KEY_BASE64 형식이 올바르지 않습니다.", exception);
        } catch (Exception exception) {
            throw new IllegalStateException("Apple private key를 읽을 수 없습니다.", exception);
        }
    }

    private byte[] decodePemIfNecessary(byte[] decoded) {
        String value = new String(decoded, StandardCharsets.UTF_8);
        if (!value.contains("-----BEGIN PRIVATE KEY-----")) {
            return decoded;
        }

        String base64Body = value
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        return Base64.getDecoder().decode(base64Body);
    }

    private void validateConfiguration() {
        if (!StringUtils.hasText(properties.clientId())
                || !StringUtils.hasText(properties.teamId())
                || !StringUtils.hasText(properties.keyId())
                || !StringUtils.hasText(properties.privateKeyBase64())
                || properties.issuer() == null
                || properties.clientSecretExpiration() == null
                || properties.clientSecretExpiration().isZero()
                || properties.clientSecretExpiration().isNegative()) {
            throw new IllegalStateException("Apple OAuth 환경변수 설정이 필요합니다.");
        }
    }
}

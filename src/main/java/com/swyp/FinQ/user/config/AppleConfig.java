package com.swyp.FinQ.user.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(AppleProperties.class)
public class AppleConfig {

    @Bean("appleIdentityTokenValidator")
    public OAuth2TokenValidator<Jwt> appleIdentityTokenValidator(AppleProperties properties) {
        OAuth2TokenValidator<Jwt> issuerAndTimestampValidator =
                JwtValidators.createDefaultWithIssuer(properties.issuer().toString());
        OAuth2TokenValidator<Jwt> audienceValidator = token ->
                token.getAudience().contains(properties.clientId())
                        ? OAuth2TokenValidatorResult.success()
                        : OAuth2TokenValidatorResult.failure(
                                new org.springframework.security.oauth2.core.OAuth2Error(
                                        "invalid_token",
                                        "Apple Identity Token audience is invalid.",
                                        null
                                )
                        );

        return new DelegatingOAuth2TokenValidator<>(issuerAndTimestampValidator, audienceValidator);
    }

    @Bean("appleIdentityTokenDecoder")
    public JwtDecoder appleIdentityTokenDecoder(
            AppleProperties properties,
            @Qualifier("appleIdentityTokenValidator") OAuth2TokenValidator<Jwt> validator
    ) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withJwkSetUri(properties.jwkSetUri().toString())
                .jwsAlgorithm(SignatureAlgorithm.RS256)
                .build();
        decoder.setJwtValidator(validator);
        return decoder;
    }

    @Bean("appleTokenRestClient")
    public RestClient appleTokenRestClient() {
        return RestClient.builder().build();
    }
}

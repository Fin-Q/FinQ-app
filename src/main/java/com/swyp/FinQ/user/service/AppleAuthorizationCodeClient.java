package com.swyp.FinQ.user.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.swyp.FinQ.global.exception.BaseException;
import com.swyp.FinQ.user.config.AppleProperties;
import com.swyp.FinQ.user.exception.AuthErrorCode;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Component
public class AppleAuthorizationCodeClient implements AppleAuthorizationCodeVerifier {

    private final RestClient restClient;
    private final AppleClientSecretGenerator clientSecretGenerator;
    private final AppleIdentityTokenVerifier identityTokenVerifier;
    private final AppleProperties properties;

    public AppleAuthorizationCodeClient(
            @Qualifier("appleTokenRestClient") RestClient restClient,
            AppleClientSecretGenerator clientSecretGenerator,
            AppleIdentityTokenVerifier identityTokenVerifier,
            AppleProperties properties
    ) {
        this.restClient = restClient;
        this.clientSecretGenerator = clientSecretGenerator;
        this.identityTokenVerifier = identityTokenVerifier;
        this.properties = properties;
    }

    @Override
    public void verify(String authorizationCode, String nonce, AppleUserIdentity expectedIdentity) {
        validateRequest(authorizationCode, nonce, expectedIdentity);

        AppleTokenResponse response = exchange(authorizationCode);
        if (response == null || !StringUtils.hasText(response.identityToken())) {
            throw BaseException.of(AuthErrorCode.INVALID_APPLE_AUTHORIZATION_CODE);
        }

        AppleUserIdentity exchangedIdentity = identityTokenVerifier.verify(response.identityToken(), nonce);
        if (!expectedIdentity.providerUserId().equals(exchangedIdentity.providerUserId())) {
            throw BaseException.of(AuthErrorCode.INVALID_APPLE_AUTHORIZATION_CODE);
        }
    }

    private AppleTokenResponse exchange(String authorizationCode) {
        LinkedMultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", properties.clientId());
        form.add("client_secret", clientSecretGenerator.generate());
        form.add("code", authorizationCode);
        form.add("grant_type", "authorization_code");

        try {
            return restClient.post()
                    .uri(properties.tokenUri())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(AppleTokenResponse.class);
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().is4xxClientError()) {
                throw BaseException.of(AuthErrorCode.INVALID_APPLE_AUTHORIZATION_CODE);
            }
            throw new BaseException(AuthErrorCode.APPLE_AUTH_SERVER_UNAVAILABLE, exception);
        } catch (RestClientException exception) {
            throw new BaseException(AuthErrorCode.APPLE_AUTH_SERVER_UNAVAILABLE, exception);
        }
    }

    private void validateRequest(
            String authorizationCode,
            String nonce,
            AppleUserIdentity expectedIdentity
    ) {
        if (!StringUtils.hasText(authorizationCode)
                || !StringUtils.hasText(nonce)
                || expectedIdentity == null
                || !StringUtils.hasText(expectedIdentity.providerUserId())) {
            throw BaseException.of(AuthErrorCode.INVALID_APPLE_AUTHORIZATION_CODE);
        }
        if (!StringUtils.hasText(properties.clientId()) || properties.tokenUri() == null) {
            throw new IllegalStateException("Apple OAuth 환경변수 설정이 필요합니다.");
        }
    }

    private record AppleTokenResponse(
            @JsonProperty("id_token") String identityToken
    ) {
    }
}

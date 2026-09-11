package com.swyp.FinQ.user.service;

import com.swyp.FinQ.global.exception.BaseException;
import com.swyp.FinQ.user.config.AppleProperties;
import com.swyp.FinQ.user.exception.UserErrorCode;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class AppleTokenRevokeClient implements AppleTokenRevoker {

    private final RestClient restClient;
    private final AppleClientSecretGenerator clientSecretGenerator;
    private final AppleProperties properties;

    public AppleTokenRevokeClient(
            @Qualifier("appleTokenRestClient") RestClient restClient,
            AppleClientSecretGenerator clientSecretGenerator,
            AppleProperties properties
    ) {
        this.restClient = restClient;
        this.clientSecretGenerator = clientSecretGenerator;
        this.properties = properties;
    }

    @Override
    public void revoke(String refreshToken) {
        validateRequest(refreshToken);

        LinkedMultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", properties.clientId());
        form.add("client_secret", clientSecretGenerator.generate());
        form.add("token", refreshToken);
        form.add("token_type_hint", "refresh_token");

        try {
            restClient.post()
                    .uri(properties.revokeUri())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException exception) {
            throw new BaseException(UserErrorCode.SOCIAL_ACCOUNT_UNLINK_FAILED, exception);
        }
    }

    private void validateRequest(String refreshToken) {
        if (!StringUtils.hasText(properties.clientId()) || properties.revokeUri() == null) {
            throw new IllegalStateException("Apple OAuth 환경변수 설정이 필요합니다.");
        }
        if (!StringUtils.hasText(refreshToken)) {
            throw BaseException.of(UserErrorCode.SOCIAL_ACCOUNT_UNLINK_FAILED);
        }
    }
}

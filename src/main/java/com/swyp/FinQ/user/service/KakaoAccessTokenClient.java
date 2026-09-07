package com.swyp.FinQ.user.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.swyp.FinQ.global.exception.BaseException;
import com.swyp.FinQ.user.config.KakaoProperties;
import com.swyp.FinQ.user.exception.AuthErrorCode;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Component
public class KakaoAccessTokenClient implements KakaoAccessTokenVerifier {

    private final RestClient restClient;
    private final String expectedAppId;

    public KakaoAccessTokenClient(RestClient.Builder restClientBuilder, KakaoProperties properties) {
        this.restClient = restClientBuilder
                .baseUrl(properties.apiBaseUrl().toString())
                .build();
        this.expectedAppId = properties.appId();
    }

    @Override
    public KakaoUserIdentity verify(String accessToken) {
        validateConfiguration();
        if (!StringUtils.hasText(accessToken)) {
            throw BaseException.of(AuthErrorCode.INVALID_KAKAO_ACCESS_TOKEN);
        }

        KakaoTokenInfoResponse response = retrieveTokenInfo(accessToken);
        if (response == null
                || response.id() == null
                || response.appId() == null
                || response.expiresIn() == null
                || response.expiresIn() <= 0
                || !expectedAppId.equals(String.valueOf(response.appId()))) {
            throw BaseException.of(AuthErrorCode.INVALID_KAKAO_ACCESS_TOKEN);
        }

        return new KakaoUserIdentity(String.valueOf(response.id()));
    }

    private KakaoTokenInfoResponse retrieveTokenInfo(String accessToken) {
        try {
            return restClient.get()
                    .uri("/v1/user/access_token_info")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .body(KakaoTokenInfoResponse.class);
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().is4xxClientError()) {
                throw BaseException.of(AuthErrorCode.INVALID_KAKAO_ACCESS_TOKEN);
            }
            throw new BaseException(AuthErrorCode.KAKAO_AUTH_SERVER_UNAVAILABLE, exception);
        } catch (RestClientException exception) {
            throw new BaseException(AuthErrorCode.KAKAO_AUTH_SERVER_UNAVAILABLE, exception);
        }
    }

    private void validateConfiguration() {
        if (!StringUtils.hasText(expectedAppId)) {
            throw new IllegalStateException("KAKAO_APP_ID 환경변수가 필요합니다.");
        }
    }

    private record KakaoTokenInfoResponse(
            Long id,
            @JsonProperty("expires_in") Long expiresIn,
            @JsonProperty("app_id") Long appId
    ) {
    }
}

package com.swyp.FinQ.user.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swyp.FinQ.global.exception.BaseException;
import com.swyp.FinQ.user.config.KakaoProperties;
import com.swyp.FinQ.user.exception.UserErrorCode;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.io.IOException;

@Component
public class KakaoAccountUnlinkClient implements KakaoAccountUnlinker {

    private static final int KAKAO_ALREADY_UNLINKED_CODE = -101;

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String adminKey;

    public KakaoAccountUnlinkClient(
            RestClient kakaoRestClient,
            ObjectMapper objectMapper,
            KakaoProperties properties
    ) {
        this.restClient = kakaoRestClient;
        this.objectMapper = objectMapper;
        this.adminKey = properties.adminKey();
    }

    @Override
    public void unlink(String providerUserId) {
        validateRequest(providerUserId);

        LinkedMultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("target_id_type", "user_id");
        form.add("target_id", providerUserId);

        try {
            KakaoUnlinkResponse response = restClient.post()
                    .uri("/v1/user/unlink")
                    .header(HttpHeaders.AUTHORIZATION, "KakaoAK " + adminKey)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(KakaoUnlinkResponse.class);
            if (response == null || response.id() == null
                    || !providerUserId.equals(String.valueOf(response.id()))) {
                throw BaseException.of(UserErrorCode.SOCIAL_ACCOUNT_UNLINK_FAILED);
            }
        } catch (RestClientResponseException exception) {
            if (isAlreadyUnlinked(exception)) {
                return;
            }
            throw new BaseException(UserErrorCode.SOCIAL_ACCOUNT_UNLINK_FAILED, exception);
        } catch (RestClientException exception) {
            throw new BaseException(UserErrorCode.SOCIAL_ACCOUNT_UNLINK_FAILED, exception);
        }
    }

    private void validateRequest(String providerUserId) {
        if (!StringUtils.hasText(adminKey)) {
            throw new IllegalStateException("KAKAO_ADMIN_KEY 환경변수가 필요합니다.");
        }
        if (!StringUtils.hasText(providerUserId)) {
            throw BaseException.of(UserErrorCode.SOCIAL_ACCOUNT_UNLINK_FAILED);
        }
    }

    private boolean isAlreadyUnlinked(RestClientResponseException exception) {
        if (!exception.getStatusCode().is4xxClientError()) {
            return false;
        }
        try {
            return objectMapper.readTree(exception.getResponseBodyAsByteArray())
                    .path("code")
                    .asInt() == KAKAO_ALREADY_UNLINKED_CODE;
        } catch (IOException ignored) {
            return false;
        }
    }

    private record KakaoUnlinkResponse(Long id) {
    }
}

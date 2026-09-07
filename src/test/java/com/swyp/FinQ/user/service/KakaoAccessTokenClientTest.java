package com.swyp.FinQ.user.service;

import com.swyp.FinQ.global.exception.BaseException;
import com.swyp.FinQ.user.config.KakaoProperties;
import com.swyp.FinQ.user.exception.AuthErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withUnauthorizedRequest;

class KakaoAccessTokenClientTest {

    private MockRestServiceServer server;
    private KakaoAccessTokenClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        client = new KakaoAccessTokenClient(
                builder,
                new KakaoProperties("1563928", URI.create("https://kapi.kakao.com"))
        );
    }

    @Test
    void 유효한_토큰이면_Kakao_사용자_ID를_반환한다() {
        server.expect(once(), requestTo("https://kapi.kakao.com/v1/user/access_token_info"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer kakao-access-token"))
                .andRespond(withSuccess("""
                        {
                          "id": 123456789,
                          "expires_in": 7199,
                          "app_id": 1563928
                        }
                        """, MediaType.APPLICATION_JSON));

        KakaoUserIdentity identity = client.verify("kakao-access-token");

        assertThat(identity.providerUserId()).isEqualTo("123456789");
        server.verify();
    }

    @Test
    void 다른_앱에서_발급된_토큰이면_거부한다() {
        server.expect(once(), requestTo("https://kapi.kakao.com/v1/user/access_token_info"))
                .andRespond(withSuccess("""
                        {
                          "id": 123456789,
                          "expires_in": 7199,
                          "app_id": 9999999
                        }
                        """, MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> client.verify("other-app-token"))
                .isInstanceOfSatisfying(BaseException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(AuthErrorCode.INVALID_KAKAO_ACCESS_TOKEN));
        server.verify();
    }

    @Test
    void 만료된_토큰이면_거부한다() {
        server.expect(once(), requestTo("https://kapi.kakao.com/v1/user/access_token_info"))
                .andRespond(withSuccess("""
                        {
                          "id": 123456789,
                          "expires_in": 0,
                          "app_id": 1563928
                        }
                        """, MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> client.verify("expired-token"))
                .isInstanceOfSatisfying(BaseException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(AuthErrorCode.INVALID_KAKAO_ACCESS_TOKEN));
        server.verify();
    }

    @Test
    void Kakao가_토큰을_거부하면_인증_오류로_변환한다() {
        server.expect(once(), requestTo("https://kapi.kakao.com/v1/user/access_token_info"))
                .andRespond(withUnauthorizedRequest());

        assertThatThrownBy(() -> client.verify("invalid-token"))
                .isInstanceOfSatisfying(BaseException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(AuthErrorCode.INVALID_KAKAO_ACCESS_TOKEN));
        server.verify();
    }

    @Test
    void Kakao_서버_오류는_서비스_불가_오류로_변환한다() {
        server.expect(once(), requestTo("https://kapi.kakao.com/v1/user/access_token_info"))
                .andRespond(withServerError());

        assertThatThrownBy(() -> client.verify("valid-token"))
                .isInstanceOfSatisfying(BaseException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(AuthErrorCode.KAKAO_AUTH_SERVER_UNAVAILABLE));
        server.verify();
    }
}

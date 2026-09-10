package com.swyp.FinQ.user.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swyp.FinQ.global.exception.BaseException;
import com.swyp.FinQ.user.config.KakaoProperties;
import com.swyp.FinQ.user.exception.UserErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class KakaoAccountUnlinkClientTest {

    private static final String PROVIDER_USER_ID = "123456789";

    private MockRestServiceServer server;
    private KakaoAccountUnlinkClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        client = new KakaoAccountUnlinkClient(
                builder.baseUrl("https://kapi.kakao.com").build(),
                new ObjectMapper(),
                new KakaoProperties(
                        "1563928",
                        "kakao-admin-key",
                        URI.create("https://kapi.kakao.com")
                )
        );
    }

    @Test
    void Admin_Key와_사용자_ID로_Kakao_연결을_해제한다() {
        LinkedMultiValueMap<String, String> expectedForm = new LinkedMultiValueMap<>();
        expectedForm.add("target_id_type", "user_id");
        expectedForm.add("target_id", PROVIDER_USER_ID);

        server.expect(once(), requestTo("https://kapi.kakao.com/v1/user/unlink"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "KakaoAK kakao-admin-key"))
                .andExpect(content().contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(content().formData(expectedForm))
                .andRespond(withSuccess("{\"id\":123456789}", MediaType.APPLICATION_JSON));

        assertThatCode(() -> client.unlink(PROVIDER_USER_ID)).doesNotThrowAnyException();
        server.verify();
    }

    @Test
    void 이미_연결_해제된_Kakao_사용자는_성공으로_처리한다() {
        server.expect(once(), requestTo("https://kapi.kakao.com/v1/user/unlink"))
                .andRespond(withBadRequest()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"msg\":\"NotExistUserException\",\"code\":-101}"));

        assertThatCode(() -> client.unlink(PROVIDER_USER_ID)).doesNotThrowAnyException();
        server.verify();
    }

    @Test
    void Kakao_인증_오류는_연결_해제_실패로_변환한다() {
        server.expect(once(), requestTo("https://kapi.kakao.com/v1/user/unlink"))
                .andRespond(withBadRequest()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"msg\":\"BadParameterException\",\"code\":-2}"));

        assertUnlinkFailed();
        server.verify();
    }

    @Test
    void Kakao_서버_오류는_연결_해제_실패로_변환한다() {
        server.expect(once(), requestTo("https://kapi.kakao.com/v1/user/unlink"))
                .andRespond(withServerError());

        assertUnlinkFailed();
        server.verify();
    }

    @Test
    void 응답의_사용자_ID가_다르면_연결_해제_실패로_처리한다() {
        server.expect(once(), requestTo("https://kapi.kakao.com/v1/user/unlink"))
                .andRespond(withSuccess("{\"id\":987654321}", MediaType.APPLICATION_JSON));

        assertUnlinkFailed();
        server.verify();
    }

    private void assertUnlinkFailed() {
        assertThatThrownBy(() -> client.unlink(PROVIDER_USER_ID))
                .isInstanceOfSatisfying(BaseException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(UserErrorCode.SOCIAL_ACCOUNT_UNLINK_FAILED));
    }
}

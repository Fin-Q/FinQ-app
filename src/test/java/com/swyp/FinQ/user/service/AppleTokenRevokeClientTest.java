package com.swyp.FinQ.user.service;

import com.swyp.FinQ.global.exception.BaseException;
import com.swyp.FinQ.user.config.AppleProperties;
import com.swyp.FinQ.user.exception.UserErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class AppleTokenRevokeClientTest {

    private static final String REFRESH_TOKEN = "apple-refresh-token";

    private MockRestServiceServer server;
    private AppleTokenRevokeClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        AppleClientSecretGenerator clientSecretGenerator = mock(AppleClientSecretGenerator.class);
        given(clientSecretGenerator.generate()).willReturn("generated-client-secret");
        client = new AppleTokenRevokeClient(
                builder.build(),
                clientSecretGenerator,
                properties()
        );
    }

    @Test
    void Apple_Refresh_Token을_폐기한다() {
        LinkedMultiValueMap<String, String> expectedForm = new LinkedMultiValueMap<>();
        expectedForm.add("client_id", "com.finq.app");
        expectedForm.add("client_secret", "generated-client-secret");
        expectedForm.add("token", REFRESH_TOKEN);
        expectedForm.add("token_type_hint", "refresh_token");

        server.expect(once(), requestTo("https://appleid.apple.com/auth/revoke"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(content().formData(expectedForm))
                .andRespond(withSuccess());

        assertThatCode(() -> client.revoke(REFRESH_TOKEN)).doesNotThrowAnyException();
        server.verify();
    }

    @Test
    void Apple이_요청을_거부하면_연결_해제_실패로_변환한다() {
        server.expect(once(), requestTo("https://appleid.apple.com/auth/revoke"))
                .andRespond(withBadRequest());

        assertRevokeFailed();
        server.verify();
    }

    @Test
    void Apple_서버_오류는_연결_해제_실패로_변환한다() {
        server.expect(once(), requestTo("https://appleid.apple.com/auth/revoke"))
                .andRespond(withServerError());

        assertRevokeFailed();
        server.verify();
    }

    private void assertRevokeFailed() {
        assertThatThrownBy(() -> client.revoke(REFRESH_TOKEN))
                .isInstanceOfSatisfying(BaseException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(UserErrorCode.SOCIAL_ACCOUNT_UNLINK_FAILED));
    }

    private AppleProperties properties() {
        return new AppleProperties(
                "com.finq.app",
                "apple-team-id",
                "apple-key-id",
                "unused-private-key",
                URI.create("https://appleid.apple.com"),
                URI.create("https://appleid.apple.com/auth/keys"),
                URI.create("https://appleid.apple.com/auth/token"),
                URI.create("https://appleid.apple.com/auth/revoke"),
                Duration.ofMinutes(5)
        );
    }
}

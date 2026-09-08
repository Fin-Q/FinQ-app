package com.swyp.FinQ.user.service;

import com.swyp.FinQ.global.exception.BaseException;
import com.swyp.FinQ.user.config.AppleProperties;
import com.swyp.FinQ.user.exception.AuthErrorCode;
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

class AppleAuthorizationCodeClientTest {

    private static final String AUTHORIZATION_CODE = "single-use-code";
    private static final String NONCE = "raw-nonce";
    private static final AppleUserIdentity EXPECTED_IDENTITY = new AppleUserIdentity("apple-user-id");

    private MockRestServiceServer server;
    private AppleClientSecretGenerator clientSecretGenerator;
    private AppleIdentityTokenVerifier identityTokenVerifier;
    private AppleAuthorizationCodeClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        clientSecretGenerator = mock(AppleClientSecretGenerator.class);
        identityTokenVerifier = mock(AppleIdentityTokenVerifier.class);
        given(clientSecretGenerator.generate()).willReturn("generated-client-secret");

        client = new AppleAuthorizationCodeClient(
                builder.build(),
                clientSecretGenerator,
                identityTokenVerifier,
                properties()
        );
    }

    @Test
    void authorization_code를_교환하고_동일한_Apple_사용자인지_검증한다() {
        LinkedMultiValueMap<String, String> expectedForm = new LinkedMultiValueMap<>();
        expectedForm.add("client_id", "com.finq.app");
        expectedForm.add("client_secret", "generated-client-secret");
        expectedForm.add("code", AUTHORIZATION_CODE);
        expectedForm.add("grant_type", "authorization_code");

        server.expect(once(), requestTo("https://appleid.apple.com/auth/token"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(content().formData(expectedForm))
                .andRespond(withSuccess("""
                        {"id_token":"exchanged-identity-token"}
                        """, MediaType.APPLICATION_JSON));
        given(identityTokenVerifier.verify("exchanged-identity-token", NONCE))
                .willReturn(EXPECTED_IDENTITY);

        client.verify(AUTHORIZATION_CODE, NONCE, EXPECTED_IDENTITY);

        server.verify();
    }

    @Test
    void 다른_사용자의_identity_token이_반환되면_거부한다() {
        server.expect(once(), requestTo("https://appleid.apple.com/auth/token"))
                .andRespond(withSuccess("""
                        {"id_token":"other-identity-token"}
                        """, MediaType.APPLICATION_JSON));
        given(identityTokenVerifier.verify("other-identity-token", NONCE))
                .willReturn(new AppleUserIdentity("other-apple-user-id"));

        assertInvalidAuthorizationCode();
        server.verify();
    }

    @Test
    void Apple이_authorization_code를_거부하면_인증_오류로_변환한다() {
        server.expect(once(), requestTo("https://appleid.apple.com/auth/token"))
                .andRespond(withBadRequest());

        assertInvalidAuthorizationCode();
        server.verify();
    }

    @Test
    void Apple_서버_오류는_서비스_불가_오류로_변환한다() {
        server.expect(once(), requestTo("https://appleid.apple.com/auth/token"))
                .andRespond(withServerError());

        assertThatThrownBy(() -> client.verify(AUTHORIZATION_CODE, NONCE, EXPECTED_IDENTITY))
                .isInstanceOfSatisfying(BaseException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(AuthErrorCode.APPLE_AUTH_SERVER_UNAVAILABLE));
        server.verify();
    }

    @Test
    void authorization_code가_비어_있으면_Apple을_호출하지_않는다() {
        assertThatThrownBy(() -> client.verify(" ", NONCE, EXPECTED_IDENTITY))
                .isInstanceOfSatisfying(BaseException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(AuthErrorCode.INVALID_APPLE_AUTHORIZATION_CODE));
        server.verify();
    }

    private void assertInvalidAuthorizationCode() {
        assertThatThrownBy(() -> client.verify(AUTHORIZATION_CODE, NONCE, EXPECTED_IDENTITY))
                .isInstanceOfSatisfying(BaseException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(AuthErrorCode.INVALID_APPLE_AUTHORIZATION_CODE));
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
                Duration.ofMinutes(5)
        );
    }
}

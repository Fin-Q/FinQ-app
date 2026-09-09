package com.swyp.FinQ.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swyp.FinQ.global.exception.BaseException;
import com.swyp.FinQ.global.security.token.JwtTokenProvider;
import com.swyp.FinQ.support.MySqlContainerSupport;
import com.swyp.FinQ.user.domain.*;
import com.swyp.FinQ.user.exception.AuthErrorCode;
import com.swyp.FinQ.user.repository.*;
import com.swyp.FinQ.user.service.KakaoAccessTokenVerifier;
import com.swyp.FinQ.user.service.KakaoUserIdentity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.*;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SocialAccountControllerTest extends MySqlContainerSupport {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired UserRepository users;
    @Autowired SocialAccountRepository accounts;
    @Autowired RefreshTokenRepository sessions;
    @Autowired JwtTokenProvider tokens;
    @Autowired PlatformTransactionManager transactions;
    @MockitoBean KakaoAccessTokenVerifier verifier;
    private final List<Long> createdUsers = new ArrayList<>();
    private static final String PATH = "/users/me/social-accounts/kakao";
    private static final String BODY = "{\"kakaoAccessToken\":\"valid-token\"}";

    @AfterEach
    void cleanup() {
        new TransactionTemplate(transactions).executeWithoutResult(status -> {
            accounts.findAll().stream().filter(a -> createdUsers.contains(a.getUser().getId()))
                    .forEach(accounts::delete);
            accounts.flush();
            createdUsers.forEach(id -> { sessions.deleteAllByUserId(id); users.deleteById(id); });
        });
    }

    @Test
    void linksExistingUserAndSocialLoginPreservesIdentity() throws Exception {
        User user = user();
        String providerId = UUID.randomUUID().toString();
        given(verifier.verify("valid-token")).willReturn(new KakaoUserIdentity(providerId));
        String response = mvc.perform(post(PATH).header("Authorization", bearer(user))
                        .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Kakao 계정 연동에 성공했습니다."))
                .andExpect(jsonPath("$.data.socialProvider").value("KAKAO"))
                .andExpect(jsonPath("$.data.linkedAt").value(org.hamcrest.Matchers.endsWith("+09:00")))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andReturn().getResponse().getContentAsString();
        String linkedAt = json.readTree(response).path("data").path("linkedAt").asText();
        mvc.perform(post(PATH).header("Authorization", bearer(user))
                        .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.linkedAt").value(linkedAt));
        mvc.perform(post("/auth/social/kakao").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value(user.getId().toString()))
                .andExpect(jsonPath("$.data.isNewUser").value(false));
        User after = users.findById(user.getId()).orElseThrow();
        assertThat(after.getEmail()).isEqualTo(user.getEmail());
        assertThat(after.getNickname()).isEqualTo(user.getNickname());
        assertThat(after.getPassword()).isEqualTo(user.getPassword());
        assertThat(after.getTotalXp()).isEqualTo(80);
        assertThat(accounts.findByProviderAndProviderUserId(SocialProvider.KAKAO, providerId)
                .orElseThrow().getUser().getId()).isEqualTo(user.getId());
    }

    @Test
    void refusesMovingAnotherUsersAccount() throws Exception {
        User owner = user();
        User other = user();
        String id = UUID.randomUUID().toString();
        accounts.saveAndFlush(SocialAccount.builder().user(owner).provider(SocialProvider.KAKAO).providerUserId(id).build());
        given(verifier.verify("valid-token")).willReturn(new KakaoUserIdentity(id));
        mvc.perform(post(PATH).header("Authorization", bearer(other)).contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("USER_SOCIAL_ACCOUNT_LINK_CONFLICT"));
        assertThat(accounts.findByProviderAndProviderUserId(SocialProvider.KAKAO, id).orElseThrow().getUser().getId())
                .isEqualTo(owner.getId());
    }

    @Test
    void refusesReplacingExistingProvider() throws Exception {
        User user = user();
        String oldId = UUID.randomUUID().toString();
        String newId = UUID.randomUUID().toString();
        accounts.saveAndFlush(SocialAccount.builder().user(user).provider(SocialProvider.KAKAO).providerUserId(oldId).build());
        given(verifier.verify("valid-token")).willReturn(new KakaoUserIdentity(newId));
        mvc.perform(post(PATH).header("Authorization", bearer(user)).contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isConflict());
        assertThat(accounts.findByProviderAndProviderUserId(SocialProvider.KAKAO, newId)).isEmpty();
    }

    @Test
    void rejectsInvalidProviderToken() throws Exception {
        User user = user();
        given(verifier.verify("valid-token")).willThrow(BaseException.of(AuthErrorCode.INVALID_KAKAO_ACCESS_TOKEN));
        mvc.perform(post(PATH).header("Authorization", bearer(user)).contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isUnauthorized());
        assertThat(accounts.existsByUserIdAndProvider(user.getId(), SocialProvider.KAKAO)).isFalse();
    }

    @Test
    void rejectsUnauthenticatedLinkBeforeProviderVerification() throws Exception {
        mvc.perform(post(PATH).contentType(MediaType.APPLICATION_JSON).content(BODY)).andExpect(status().isUnauthorized());
        verifyNoInteractions(verifier);
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"kakaoAccessToken\":null}", "{\"kakaoAccessToken\":\" \"}"})
    void validatesToken(String body) throws Exception {
        mvc.perform(post(PATH).header("Authorization", bearer(user())).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(verifier);
    }

    @Test
    void concurrentUsersCannotClaimSameSocialAccount() throws Exception {
        User firstUser = user();
        User secondUser = user();
        String providerId = UUID.randomUUID().toString();
        given(verifier.verify("valid-token")).willReturn(new KakaoUserIdentity(providerId));
        try (var pool = Executors.newFixedThreadPool(2)) {
            CountDownLatch start = new CountDownLatch(1);
            var first = pool.submit(() -> linkAfter(start, firstUser));
            var second = pool.submit(() -> linkAfter(start, secondUser));
            start.countDown();
            assertThat(List.of(first.get(20, TimeUnit.SECONDS), second.get(20, TimeUnit.SECONDS)))
                    .containsExactlyInAnyOrder(200, 409);
        }
    }

    private int linkAfter(CountDownLatch start, User user) throws Exception {
        if (!start.await(10, TimeUnit.SECONDS)) throw new IllegalStateException("Start timed out");
        return mvc.perform(post(PATH).header("Authorization", bearer(user)).contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andReturn().getResponse().getStatus();
    }

    private User user() {
        User user = users.saveAndFlush(User.builder().email(UUID.randomUUID() + "@example.com")
                .password("unchanged-password-hash").nickname("existing-user").totalXp(80)
                .profileImageCode(ProfileImageCode.PROFILE_01).build());
        createdUsers.add(user.getId());
        return user;
    }

    private String bearer(User user) { return "Bearer " + tokens.issue(user.getId()).accessToken(); }
}

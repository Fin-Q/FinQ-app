package com.swyp.FinQ.global.contract;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.swyp.FinQ.content.dto.res.ContentDetailResponse;
import com.swyp.FinQ.home.dto.res.HomeResponse;
import com.swyp.FinQ.learning.dto.res.QuizAnswerResponse;
import com.swyp.FinQ.learning.dto.res.ContentAnswerResponse;
import com.swyp.FinQ.notification.dto.req.PushTokenRegistrationRequest;
import com.swyp.FinQ.notification.dto.res.PushTokenUnregistrationResponse;
import com.swyp.FinQ.notification.domain.PushPlatform;
import com.swyp.FinQ.user.domain.OnboardingStatus;
import com.swyp.FinQ.user.dto.res.TokenRefreshResponse;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ApiFieldContractTest {

    @Test
    void includesExplicitNullResultsForIncorrectAndRepeatedAnswers() {
        for (ContentAnswerResponse response : java.util.List.of(
                ContentAnswerResponse.incorrect("해설", "A", "B"),
                ContentAnswerResponse.correct("해설", "B", "B", "CONTENT_COMPLETED", null))) {
            JsonNode json = objectMapper.valueToTree(response);
            assertThat(json.has("contentResult")).isTrue();
            assertThat(json.path("contentResult").isNull()).isTrue();
        }
        for (QuizAnswerResponse response : java.util.List.of(
                QuizAnswerResponse.incorrect("해설", "A", "B", false),
                QuizAnswerResponse.correct("해설", "B", "B", true, null))) {
            JsonNode json = objectMapper.valueToTree(response);
            assertThat(json.has("categoryResult")).isTrue();
            assertThat(json.path("categoryResult").isNull()).isTrue();
        }
    }

    private final ObjectMapper objectMapper = JsonMapper.builder()
            .findAndAddModules()
            .build();

    @Test
    void serializesAuthenticationResponseFieldNames() throws Exception {
        TokenRefreshResponse response = new TokenRefreshResponse(
                "access-token",
                "refresh-token",
                "Bearer",
                3600L,
                1209600L
        );

        JsonNode json = objectMapper.valueToTree(response);

        assertThat(json.path("accessTokenExpiresIn").asLong()).isEqualTo(3600L);
        assertThat(json.path("refreshTokenExpiresIn").asLong()).isEqualTo(1209600L);
        assertThat(objectMapper.writeValueAsString(OnboardingStatus.INTEREST_SELECTION))
                .isEqualTo("\"INTEREST_SELECTION\"");
    }

    @Test
    void serializesHomeQuestionWithNumericContentIdAndCategoryCode() {
        HomeResponse.QuestionCard question = new HomeResponse.QuestionCard(
                42L,
                "SAL",
                "월급 관리",
                "월급 관리의 시작",
                "INCOMPLETE"
        );

        JsonNode json = objectMapper.valueToTree(question);

        assertThat(json.path("contentId").isIntegralNumber()).isTrue();
        assertThat(json.path("contentId").asLong()).isEqualTo(42L);
        assertThat(json.path("categoryCode").asText()).isEqualTo("SAL");
        assertThat(json.has("categoryId")).isFalse();
    }

    @Test
    void serializesLearningAndContentFieldNames() {
        QuizAnswerResponse quizAnswer = QuizAnswerResponse.correct(
                "해설",
                "B",
                "B",
                true,
                null
        );
        ContentDetailResponse.BodyBlockResponse comparisonBody =
                new ContentDetailResponse.BodyBlockResponse(
                        "비교",
                        "비교 설명",
                        null,
                        "https://example.com/image.png",
                        "https://example.com/table.png"
                );

        JsonNode quizJson = objectMapper.valueToTree(quizAnswer);
        JsonNode bodyJson = objectMapper.valueToTree(comparisonBody);

        assertThat(quizJson.path("isLastQuestion").asBoolean()).isTrue();
        assertThat(quizJson.has("lastQuestion")).isFalse();
        assertThat(bodyJson.path("tableImageUrl").asText())
                .isEqualTo("https://example.com/table.png");
        assertThat(bodyJson.has("tableData")).isFalse();
    }

    @Test
    void serializesNotificationRequestAndResponseFieldNames() {
        PushTokenRegistrationRequest request = new PushTokenRegistrationRequest(
                "fcm-token",
                PushPlatform.IOS
        );
        PushTokenUnregistrationResponse response = new PushTokenUnregistrationResponse(
                "device-1",
                OffsetDateTime.parse("2026-09-09T15:00:00+09:00")
        );

        JsonNode requestJson = objectMapper.valueToTree(request);
        JsonNode responseJson = objectMapper.valueToTree(response);

        assertThat(requestJson.path("fcmToken").asText()).isEqualTo("fcm-token");
        assertThat(requestJson.path("platform").asText()).isEqualTo("IOS");
        assertThat(requestJson.has("pushToken")).isFalse();
        assertThat(requestJson.has("platorm")).isFalse();
        assertThat(responseJson.has("unregisteredAt")).isTrue();
        assertThat(responseJson.has("updatedAt")).isFalse();
    }
}

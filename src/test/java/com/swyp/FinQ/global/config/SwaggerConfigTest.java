package com.swyp.FinQ.global.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swyp.FinQ.support.MySqlContainerSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.core.env.Environment;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SwaggerConfigTest extends MySqlContainerSupport {

    private static final Set<String> HTTP_METHODS = Set.of("get", "post", "put", "patch", "delete");

    private static final Set<String> PUBLIC_OPERATIONS = Set.of(
            "get /auth/agreements",
            "post /auth/sign-up",
            "post /auth/login",
            "post /auth/social/apple",
            "post /auth/social/kakao",
            "post /auth/token/refresh",
            "post /auth/password-reset/verifications",
            "post /auth/password-reset/verifications/confirm",
            "post /auth/password-reset"
    );

    private static final Set<String> EXPECTED_OPERATION_IDS = Set.of(
            "USER-001", "USER-002", "USER-003", "USER-004", "USER-005", "USER-006",
            "USER-007", "USER-008", "USER-009", "USER-010", "USER-011", "USER-012", "USER-013",
            "USER-014", "USER-015", "USER-016", "USER-017", "USER-018",
            "CONTENT-001", "CONTENT-002", "CONTENT-003", "HOME-001",
            "LEARNING-001", "LEARNING-002", "LEARNING-003", "REWARD-001",
            "STREAK-001", "STREAK-002", "NOTI-001", "NOTI-002", "NOTI-003"
    );

    private static final Set<String> EXPECTED_UNASSIGNED_OPERATIONS = Set.of(
            "get /auth/agreements",
            "put /users/me/interests"
    );

    private static final Map<String, String> EXPECTED_OWNERS_BY_TAG = Map.of(
            ApiTags.CONTENT, "yezanee",
            ApiTags.HOME, "yezanee",
            ApiTags.LEARNING, "yezanee",
            ApiTags.NOTI, "미지정",
            ApiTags.REWARD, "yezanee",
            ApiTags.STREAK, "이민지",
            ApiTags.USER, "이민지"
    );

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Environment environment;

    @Test
    void registersJwtBearerSecurityScheme() throws Exception {
        JsonNode apiDocs = getApiDocs();
        JsonNode bearerAuth = apiDocs.path("components").path("securitySchemes").path("bearerAuth");

        assertThat(bearerAuth.path("type").asText()).isEqualTo("http");
        assertThat(bearerAuth.path("scheme").asText()).isEqualTo("bearer");
        assertThat(bearerAuth.path("bearerFormat").asText()).isEqualTo("JWT");
    }

    @Test
    void documentsOnlyProtectedOperationsWithBearerAuthentication() throws Exception {
        List<ApiOperation> operations = getOperations(getApiDocs());

        assertThat(operations).hasSize(33);
        for (ApiOperation operation : operations) {
            boolean hasBearerSecurity = operation.document().path("security").isArray()
                    && operation.document().path("security").size() == 1
                    && operation.document().path("security").get(0).path("bearerAuth").isArray();

            assertThat(hasBearerSecurity)
                    .as("Bearer security for %s", operation.key())
                    .isEqualTo(!PUBLIC_OPERATIONS.contains(operation.key()));
        }
    }

    @Test
    void mapsNotionApiIdsWithoutDuplicatesAndMarksOnlyKnownGapsAsUnassigned() throws Exception {
        List<ApiOperation> operations = getOperations(getApiDocs());
        Set<String> operationIds = new HashSet<>();
        Set<String> unassignedOperations = new HashSet<>();

        for (ApiOperation operation : operations) {
            JsonNode operationId = operation.document().get("operationId");
            if (operationId == null || operationId.asText().isBlank()) {
                unassignedOperations.add(operation.key());
                assertThat(operation.document().path("summary").asText()).startsWith("[미지정] ");
                continue;
            }

            assertThat(operationIds.add(operationId.asText()))
                    .as("duplicate operationId: %s", operationId.asText())
                    .isTrue();
            assertThat(operation.document().path("summary").asText())
                    .startsWith("[" + operationId.asText() + "] ");
        }

        assertThat(operationIds).containsExactlyInAnyOrderElementsOf(EXPECTED_OPERATION_IDS);
        assertThat(unassignedOperations).containsExactlyInAnyOrderElementsOf(EXPECTED_UNASSIGNED_OPERATIONS);
    }

    @Test
    void exposesOnlyUppercaseDomainTags() throws Exception {
        JsonNode apiDocs = getApiDocs();
        List<ApiOperation> operations = getOperations(apiDocs);
        Set<String> tags = new HashSet<>();

        for (ApiOperation operation : operations) {
            operation.document().path("tags").forEach(tagNode -> {
                String tag = tagNode.asText();
                tags.add(tag);
                assertThat(tag).isEqualTo(tag.toUpperCase(Locale.ROOT));
            });
        }

        assertThat(tags).containsExactlyInAnyOrderElementsOf(ApiTags.ALL);

        Set<String> documentedTags = new HashSet<>();
        apiDocs.path("tags").forEach(tagNode -> documentedTags.add(tagNode.path("name").asText()));
        assertThat(documentedTags).containsExactlyInAnyOrderElementsOf(ApiTags.ALL);
    }

    @Test
    void exposesBackendOwners() throws Exception {
        for (ApiOperation operation : getOperations(getApiDocs())) {
            String tag = operation.document().path("tags").get(0).asText();
            assertThat(operation.document().path("x-owner").asText())
                    .as("backend owner for %s", operation.key())
                    .isEqualTo(EXPECTED_OWNERS_BY_TAG.get(tag));
        }
    }

    @Test
    void configuresSwaggerUiForApiExploration() {
        assertThat(environment.getProperty("springdoc.swagger-ui.operations-sorter")).isEqualTo("alpha");
        assertThat(environment.getProperty("springdoc.swagger-ui.tags-sorter")).isEqualTo("alpha");
        assertThat(environment.getProperty("springdoc.swagger-ui.filter")).isEqualTo("true");
        assertThat(environment.getProperty("springdoc.swagger-ui.doc-expansion")).isEqualTo("none");
        assertThat(environment.getProperty("springdoc.swagger-ui.display-request-duration")).isEqualTo("true");
    }

    private JsonNode getApiDocs() throws Exception {
        String response = mockMvc.perform(get("/api-docs"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response);
    }

    private List<ApiOperation> getOperations(JsonNode apiDocs) {
        List<ApiOperation> operations = new ArrayList<>();
        apiDocs.path("paths").properties().forEach(pathEntry ->
                pathEntry.getValue().properties().forEach(methodEntry -> {
                    if (HTTP_METHODS.contains(methodEntry.getKey())) {
                        operations.add(new ApiOperation(
                                pathEntry.getKey(),
                                methodEntry.getKey(),
                                methodEntry.getValue()
                        ));
                    }
                })
        );
        return operations;
    }

    private record ApiOperation(String path, String method, JsonNode document) {
        private String key() {
            return method + " " + path;
        }
    }
}

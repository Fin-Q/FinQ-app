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
            ApiTags.CONTENT, ApiOwner.YEZANEE.displayName(),
            ApiTags.HOME, ApiOwner.YEZANEE.displayName(),
            ApiTags.LEARNING, ApiOwner.YEZANEE.displayName(),
            ApiTags.NOTI, ApiOwner.UNASSIGNED.displayName(),
            ApiTags.REWARD, ApiOwner.YEZANEE.displayName(),
            ApiTags.STREAK, ApiOwner.MINJI.displayName(),
            ApiTags.USER, ApiOwner.MINJI.displayName()
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
            String owner = operation.document().path("x-owner").asText();
            assertThat(owner)
                    .as("backend owner for %s", operation.key())
                    .isEqualTo(EXPECTED_OWNERS_BY_TAG.get(tag));
            assertThat(operation.document().path("description").asText())
                    .as("owner appears first for %s", operation.key())
                    .startsWith("**BE 담당자:** " + owner);
        }
    }

    @Test
    void configuresSwaggerUiForApiExploration() {
        assertThat(environment.getProperty("springdoc.swagger-ui.operations-sorter")).isEqualTo("alpha");
        assertThat(environment.getProperty("springdoc.swagger-ui.tags-sorter")).isEqualTo("alpha");
        assertThat(environment.getProperty("springdoc.swagger-ui.filter")).isEqualTo("true");
        assertThat(environment.getProperty("springdoc.swagger-ui.doc-expansion")).isEqualTo("none");
        assertThat(environment.getProperty("springdoc.swagger-ui.display-operation-id")).isEqualTo("true");
        assertThat(environment.getProperty("springdoc.swagger-ui.display-request-duration")).isEqualTo("true");
        assertThat(environment.getProperty("springdoc.swagger-ui.deep-linking")).isEqualTo("true");
        assertThat(environment.getProperty("springdoc.swagger-ui.persist-authorization")).isEqualTo("true");
        assertThat(environment.getProperty("springdoc.swagger-ui.default-model-rendering")).isEqualTo("model");
        assertThat(environment.getProperty("springdoc.swagger-ui.show-common-extensions")).isEqualTo("true");
    }

    @Test
    void documentsUserAndNotificationDtoConstraints() throws Exception {
        JsonNode schemas = getApiDocs().path("components").path("schemas");

        JsonNode signUp = schemas.path("SignUpRequest");
        assertThat(textValues(signUp.path("required")))
                .contains("email", "password", "nickname", "agreements");
        assertThat(signUp.path("properties").path("email").path("format").asText()).isEqualTo("email");
        assertThat(signUp.path("properties").path("email").path("maxLength").asInt()).isEqualTo(255);
        assertThat(signUp.path("properties").path("password").path("minLength").asInt()).isEqualTo(8);
        assertThat(signUp.path("properties").path("password").path("maxLength").asInt()).isEqualTo(72);

        JsonNode verificationCode = schemas.path("VerificationCodeConfirmRequest")
                .path("properties").path("verificationCode");
        assertThat(verificationCode.path("pattern").asText()).isEqualTo("[0-9]{6}");
        assertThat(verificationCode.path("description").asText()).isNotBlank();

        assertEveryPropertyHasDescription(schemas.path("NotificationSettingUpdateRequest"));
        assertEveryPropertyHasDescription(schemas.path("PushTokenRegistrationRequest"));
        assertEveryPropertyHasDescription(schemas.path("NotificationSettingResponse"));
        assertEveryPropertyHasDescription(schemas.path("PushTokenRegistrationResponse"));
    }

    @Test
    void documentsCommonSuccessAndErrorResponses() throws Exception {
        JsonNode schemas = getApiDocs().path("components").path("schemas");
        assertThat(textValues(schemas.path("ErrorResponse").path("properties").path("status").path("enum")))
                .containsExactlyInAnyOrder("SUCCESS", "ERROR");
        assertEveryPropertyHasDescription(schemas.path("ErrorResponse"));
        int[] successSchemaCount = {0};
        schemas.properties().forEach(schema -> {
            if (schema.getKey().startsWith("SuccessResponse")) {
                assertEveryPropertyHasDescription(schema.getValue());
                assertThat(textValues(schema.getValue().path("properties").path("status").path("enum")))
                        .as("response status enum for %s", schema.getKey())
                        .containsExactlyInAnyOrder("SUCCESS", "ERROR");
                successSchemaCount[0]++;
            }
        });
        assertThat(successSchemaCount[0]).isPositive();

        for (ApiOperation operation : getOperations(getApiDocs())) {
            JsonNode responses = operation.document().path("responses");
            assertThat(responses.has("400")).as("400 response for %s", operation.key()).isTrue();
            assertThat(responses.has("500")).as("500 response for %s", operation.key()).isTrue();
            assertThat(responses.path("400").path("content").path("application/json")
                    .path("examples").path("example").path("value").path("status").asText())
                    .as("400 example for %s", operation.key())
                    .isEqualTo("ERROR");

            boolean shouldRequireAuthentication = !PUBLIC_OPERATIONS.contains(operation.key());
            assertThat(responses.has("401"))
                    .as("401 response for %s", operation.key())
                    .isEqualTo(shouldRequireAuthentication);
        }
    }

    @Test
    void resolvesEveryLocalSchemaReference() throws Exception {
        JsonNode apiDocs = getApiDocs();
        JsonNode schemas = apiDocs.path("components").path("schemas");
        Set<String> missingSchemas = new HashSet<>();

        collectMissingSchemaReferences(apiDocs, schemas, missingSchemas);

        assertThat(missingSchemas)
                .as("OpenAPI 문서에서 참조하지만 components.schemas에 등록되지 않은 스키마")
                .isEmpty();
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

    private Set<String> textValues(JsonNode array) {
        Set<String> values = new HashSet<>();
        array.forEach(value -> values.add(value.asText()));
        return values;
    }

    private void assertEveryPropertyHasDescription(JsonNode schema) {
        assertThat(schema.isMissingNode()).isFalse();
        schema.path("properties").properties().forEach(property ->
                assertThat(property.getValue().path("description").asText())
                        .as("description for %s", property.getKey())
                        .isNotBlank()
        );
    }

    private void collectMissingSchemaReferences(
            JsonNode node,
            JsonNode schemas,
            Set<String> missingSchemas
    ) {
        if (node.isObject()) {
            JsonNode reference = node.get("$ref");
            String schemaPrefix = "#/components/schemas/";
            if (reference != null && reference.asText().startsWith(schemaPrefix)) {
                String schemaName = reference.asText().substring(schemaPrefix.length());
                if (!schemas.has(schemaName)) {
                    missingSchemas.add(schemaName);
                }
            }
            node.elements().forEachRemaining(child ->
                    collectMissingSchemaReferences(child, schemas, missingSchemas));
            return;
        }

        if (node.isArray()) {
            node.elements().forEachRemaining(child ->
                    collectMissingSchemaReferences(child, schemas, missingSchemas));
        }
    }

    private record ApiOperation(String path, String method, JsonNode document) {
        private String key() {
            return method + " " + path;
        }
    }
}

package com.swyp.FinQ.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

  public static final String BEARER_AUTH_SCHEME = "bearerAuth";

  @Bean
  public OpenAPI openAPI() {
    return new OpenAPI()
      .components(new Components().addSecuritySchemes(
        BEARER_AUTH_SCHEME,
        new SecurityScheme()
          .type(SecurityScheme.Type.HTTP)
          .scheme("bearer")
          .bearerFormat("JWT")
          .description("JWT Access Token을 입력하세요. Bearer 접두사는 자동으로 적용됩니다.")
      ))
      .tags(List.of(
        new Tag().name(ApiTags.CONTENT).description("콘텐츠 및 지식맵 API"),
        new Tag().name(ApiTags.HOME).description("홈 화면 API"),
        new Tag().name(ApiTags.LEARNING).description("학습 채점 및 심화 퀴즈 API"),
        new Tag().name(ApiTags.NOTI).description("푸시 토큰 및 알림 설정 API"),
        new Tag().name(ApiTags.REWARD).description("보상 및 레벨 API"),
        new Tag().name(ApiTags.STREAK).description("스트릭 기록 조회 API"),
        new Tag().name(ApiTags.USER).description("회원 인증, 온보딩 및 프로필 API")
      ))
      .info(new Info()
        .title("FinQ API")
        .description("FinQ 금융 학습 서비스 API 문서")
        .version("v1.0.0"))
      .components(new Components()
        .addSecuritySchemes("bearerAuth", new SecurityScheme()
          .type(SecurityScheme.Type.HTTP)
          .scheme("bearer")
          .bearerFormat("JWT")
          .description("JWT Access Token을 입력하세요.")));
  }

  @Bean
  public OperationCustomizer apiDocumentationCustomizer() {
    return (operation, handlerMethod) -> {
      ApiDocumentation documentation = handlerMethod.getMethodAnnotation(ApiDocumentation.class);
      if (documentation == null) {
        return operation;
      }

      String apiId = documentation.id().isBlank() ? "미지정" : documentation.id();
      String owner = documentation.owner().isBlank() ? "미지정" : documentation.owner();

      operation.setSummary("[" + apiId + "] " + documentation.name());
      operation.setOperationId(documentation.id().isBlank() ? null : documentation.id());
      operation.setDescription(withOwner(operation.getDescription(), owner));
      operation.addExtension("x-owner", owner);

      if (documentation.secured()) {
        operation.setSecurity(List.of(new SecurityRequirement().addList(BEARER_AUTH_SCHEME)));
      } else {
        operation.setSecurity(null);
      }

      return operation;
    };
  }

  private String withOwner(String description, String owner) {
    String ownerDescription = "**BE 담당자:** " + owner;
    if (description == null || description.isBlank()) {
      return ownerDescription;
    }
    return description + "\n\n" + ownerDescription;
  }
}

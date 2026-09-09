package com.swyp.FinQ.content.controller;

import com.swyp.FinQ.content.domain.CategoryCode;
import com.swyp.FinQ.content.dto.res.CategoryDetailResponse;
import com.swyp.FinQ.content.dto.res.ContentDetailResponse;
import com.swyp.FinQ.content.dto.res.KnowledgeMapResponse;
import com.swyp.FinQ.content.service.ContentQueryService;
import com.swyp.FinQ.content.success.ContentSuccessCode;
import com.swyp.FinQ.global.success.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.extensions.Extension;
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Content", description = "콘텐츠 및 지식맵 API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequiredArgsConstructor
public class ContentController {

    private final ContentQueryService contentQueryService;

    @Operation(
            operationId = "CONTENT-001",
            summary = "[CONTENT-001] 지식맵 조회",
            description = "전체 카테고리 목록과 사용자별 진행률을 조회합니다.",
            extensions = @Extension(properties = @ExtensionProperty(name = "x-owner", value = "yezanee"))
    )
    @GetMapping("/knowledge-map")
    public ResponseEntity<SuccessResponse<KnowledgeMapResponse>> getKnowledgeMap(
            @AuthenticationPrincipal Jwt jwt
    ) {
        KnowledgeMapResponse response = contentQueryService.getKnowledgeMap(Long.valueOf(jwt.getSubject()));
        return SuccessResponse.of(ContentSuccessCode.KNOWLEDGE_MAP_RETRIEVED, response);
    }

    @Operation(
            operationId = "CONTENT-002",
            summary = "[CONTENT-002] 카테고리 상세 조회",
            description = "카테고리별 콘텐츠 목록과 완료 상태를 조회합니다.",
            extensions = @Extension(properties = @ExtensionProperty(name = "x-owner", value = "yezanee"))
    )
    @GetMapping("/categories/{categoryCode}")
    public ResponseEntity<SuccessResponse<CategoryDetailResponse>> getCategoryDetail(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "카테고리 코드") @PathVariable CategoryCode categoryCode
    ) {
        CategoryDetailResponse response = contentQueryService.getCategoryDetail(categoryCode, Long.valueOf(jwt.getSubject()));
        return SuccessResponse.of(ContentSuccessCode.CATEGORY_DETAIL_RETRIEVED, response);
    }

    @Operation(
            operationId = "CONTENT-003",
            summary = "[CONTENT-003] 콘텐츠 상세 조회",
            description = "본문, 핵심정리, 문제를 블록 배열로 한 번에 조회합니다.",
            extensions = @Extension(properties = @ExtensionProperty(name = "x-owner", value = "yezanee"))
    )
    @GetMapping("/contents/{contentId}")
    public ResponseEntity<SuccessResponse<ContentDetailResponse>> getContentDetail(
            @Parameter(description = "콘텐츠 ID") @PathVariable Long contentId
    ) {
        ContentDetailResponse response = contentQueryService.getContentDetail(contentId);
        return SuccessResponse.of(ContentSuccessCode.CONTENT_DETAIL_RETRIEVED, response);
    }
}
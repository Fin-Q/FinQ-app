package com.swyp.FinQ.content.controller;

import com.swyp.FinQ.content.domain.CategoryCode;
import com.swyp.FinQ.content.dto.res.CategoryDetailResponse;
import com.swyp.FinQ.content.dto.res.ContentDetailResponse;
import com.swyp.FinQ.content.dto.res.KnowledgeMapResponse;
import com.swyp.FinQ.content.service.ContentQueryService;
import com.swyp.FinQ.content.success.ContentSuccessCode;
import com.swyp.FinQ.global.config.ApiDocumentation;
import com.swyp.FinQ.global.config.ApiOwner;
import com.swyp.FinQ.global.config.ApiTags;
import com.swyp.FinQ.global.success.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = ApiTags.CONTENT, description = "콘텐츠 및 지식맵 API")
@RestController
@RequiredArgsConstructor
public class ContentController {

    private final ContentQueryService contentQueryService;

    @Operation(
            description = "전체 카테고리 목록과 진행률을 조회합니다. "
                    + "Authorization 헤더가 없으면 게스트로 처리하여 진행 상태를 초기값으로 반환합니다."
    )
    @ApiDocumentation(id = "CONTENT-001", name = "지식맵 조회", owner = ApiOwner.YEZANEE, optionalAuth = true)
    @GetMapping("/knowledge-map")
    public ResponseEntity<SuccessResponse<KnowledgeMapResponse>> getKnowledgeMap(
            @AuthenticationPrincipal Jwt jwt
    ) {
        KnowledgeMapResponse response = jwt == null
                ? contentQueryService.getGuestKnowledgeMap()
                : contentQueryService.getKnowledgeMap(Long.valueOf(jwt.getSubject()));
        return SuccessResponse.of(ContentSuccessCode.KNOWLEDGE_MAP_RETRIEVED, response);
    }

    @Operation(
            description = "카테고리별 콘텐츠 목록과 완료 상태를 조회합니다. "
                    + "Authorization 헤더가 없으면 게스트로 처리하여 완료 상태를 초기값으로 반환합니다."
    )
    @ApiDocumentation(id = "CONTENT-002", name = "카테고리 상세 조회", owner = ApiOwner.YEZANEE, optionalAuth = true)
    @GetMapping("/categories/{categoryCode}")
    public ResponseEntity<SuccessResponse<CategoryDetailResponse>> getCategoryDetail(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "카테고리 코드") @PathVariable CategoryCode categoryCode
    ) {
        CategoryDetailResponse response = jwt == null
                ? contentQueryService.getGuestCategoryDetail(categoryCode)
                : contentQueryService.getCategoryDetail(categoryCode, Long.valueOf(jwt.getSubject()));
        return SuccessResponse.of(ContentSuccessCode.CATEGORY_DETAIL_RETRIEVED, response);
    }

    @Operation(
            description = "본문, 핵심정리, 문제를 블록 배열로 한 번에 조회합니다. "
                    + "게스트는 일반 콘텐츠만 조회할 수 있습니다."
    )
    @ApiDocumentation(
            id = "CONTENT-003",
            name = "콘텐츠 상세 조회",
            owner = ApiOwner.YEZANEE,
            optionalAuth = true,
            errors = {"PREMIUM_CONTENT_ACCESS_DENIED"}
    )
    @GetMapping("/contents/{contentId}")
    public ResponseEntity<SuccessResponse<ContentDetailResponse>> getContentDetail(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "콘텐츠 ID") @PathVariable Long contentId
    ) {
        ContentDetailResponse response = jwt == null
                ? contentQueryService.getGuestContentDetail(contentId)
                : contentQueryService.getContentDetail(contentId);
        return SuccessResponse.of(ContentSuccessCode.CONTENT_DETAIL_RETRIEVED, response);
    }
}

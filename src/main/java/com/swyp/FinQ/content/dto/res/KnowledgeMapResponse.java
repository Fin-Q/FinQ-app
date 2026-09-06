package com.swyp.FinQ.content.dto.res;

import com.swyp.FinQ.content.domain.CategoryCode;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "지식맵 조회 응답")
public record KnowledgeMapResponse(
        @Schema(description = "카테고리 목록")
        List<CategoryProgress> categories
) {

    @Schema(description = "카테고리별 진행 현황")
    public record CategoryProgress(
            @Schema(description = "카테고리 ID", example = "1")
            Long categoryId,
            @Schema(description = "카테고리 코드", example = "SAL")
            CategoryCode categoryCode,
            @Schema(description = "카테고리명", example = "월급 관리")
            String categoryName,
            @Schema(description = "완료 콘텐츠 수", example = "2")
            int completedContentCount,
            @Schema(description = "전체 콘텐츠 수", example = "5")
            int totalContentCount,
            @Schema(description = "진행률 (%)", example = "40")
            int progressRate,
            @Schema(description = "카테고리 완료 여부 (심화퀴즈 전부 정답 시 true)", example = "false")
            boolean categoryCompleted
    ) {
    }
}
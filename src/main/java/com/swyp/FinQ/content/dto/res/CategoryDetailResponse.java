package com.swyp.FinQ.content.dto.res;

import com.swyp.FinQ.content.domain.CategoryCode;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "카테고리 상세 조회 응답")
public record CategoryDetailResponse(
        @Schema(description = "카테고리 ID", example = "1")
        Long categoryId,
        @Schema(description = "카테고리 코드", example = "SAL")
        CategoryCode categoryCode,
        @Schema(description = "카테고리명", example = "월급관리·저축")
        String categoryName,
        @Schema(description = "완료 콘텐츠 수", example = "2")
        int completedContentCount,
        @Schema(description = "전체 콘텐츠 수", example = "5")
        int totalContentCount,
        @Schema(description = "진행률 (%)", example = "40")
        int progressRate,
        @Schema(description = "카테고리 완료 여부 (심화퀴즈 전부 정답 시 true)", example = "false")
        boolean categoryCompleted,
        @Schema(description = "심화퀴즈 상태", example = "INCOMPLETE", allowableValues = {"INCOMPLETE", "COMPLETED"})
        String advancedQuizStatus,
        @Schema(description = "콘텐츠 목록")
        List<ContentSummary> contents,
        @Schema(description = "프리미엄 콘텐츠 목록")
        List<PremiumContentSummary> premiumContents
) {

    @Schema(description = "콘텐츠 요약 정보")
    public record ContentSummary(
            @Schema(description = "콘텐츠 ID", example = "1")
            Long contentId,
            @Schema(description = "콘텐츠 코드", example = "SAL-01")
            String contentCode,
            @Schema(description = "콘텐츠 제목", example = "월급 관리의 시작")
            String title,
            @Schema(description = "콘텐츠 소개글", example = "월급 관리, 어디서부터 시작할까요?")
            String description,
            @Schema(description = "완료 상태", example = "INCOMPLETE", allowableValues = {"INCOMPLETE", "COMPLETED"})
            String completionStatus,
            @Schema(description = "카테고리 내 콘텐츠 순서", example = "1")
            int order
    ) {
    }

    @Schema(description = "프리미엄 콘텐츠 요약 정보")
    public record PremiumContentSummary(
            @Schema(description = "프리미엄 콘텐츠 ID", example = "6")
            Long contentId,
            @Schema(description = "프리미엄 콘텐츠 제목", example = "프리미엄: 자동이체 고급 전략")
            String title
    ) {
    }
}
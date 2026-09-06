package com.swyp.FinQ.content.dto.res;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "학습 콘텐츠 전체 조회 응답")
public record ContentDetailResponse(
        @Schema(description = "콘텐츠 ID", example = "1")
        Long contentId,
        @Schema(description = "카테고리 ID", example = "1")
        Long categoryId,
        @Schema(description = "카테고리명", example = "월급 관리")
        String categoryName,
        @Schema(description = "콘텐츠 제목", example = "월급 관리의 시작")
        String title,
        @Schema(description = "출처", example = "금융감독원")
        String source,
        @Schema(description = "기준일 (YYYY-MM-DD)", example = "2026-01-01")
        LocalDate referenceDate,
        @Schema(description = "카테고리 내 현재 콘텐츠 순서", example = "1")
        int contentOrder,
        @Schema(description = "카테고리 전체 콘텐츠 수", example = "4")
        int totalContentsInCategory,
        @Schema(description = "블록 목록")
        List<BlockResponse> blocks
) {

    @Schema(description = "콘텐츠 블록")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record BlockResponse(
            @Schema(description = "블록 순서", example = "1")
            int order,
            @Schema(description = "블록 유형", example = "BODY", allowableValues = {"BODY", "SUMMARY", "QUESTION"})
            String blockType,
            @Schema(description = "본문 유형 (BODY 전용)", example = "EXPLANATION", allowableValues = {"EXPLANATION", "CASE", "COMPARISON"})
            String bodyType,
            @Schema(description = "본문 데이터 (BODY 전용)")
            BodyBlockResponse body,
            @Schema(description = "핵심 정리 내용 (SUMMARY 전용)")
            String summaryContent,
            @Schema(description = "문제 ID (QUESTION 전용)", example = "1")
            Long questionId,
            @Schema(description = "문제 단계 (QUESTION 전용)", example = "P1", allowableValues = {"P1", "P2", "F"})
            String questionStage,
            @Schema(description = "문제 유형 (QUESTION 전용)", example = "OX", allowableValues = {"OX", "SINGLE_CHOICE"})
            String questionType,
            @Schema(description = "문제 본문 (QUESTION 전용)")
            String questionBody,
            @Schema(description = "보기 목록 (QUESTION 전용)")
            List<OptionResponse> options
    ) {

        public static BlockResponse ofBody(int order, String bodyType, BodyBlockResponse body) {
            return new BlockResponse(order, "BODY", bodyType, body, null, null, null, null, null, null);
        }

        public static BlockResponse ofSummary(int order, String summaryContent) {
            return new BlockResponse(order, "SUMMARY", null, null, summaryContent, null, null, null, null, null);
        }

        public static BlockResponse ofQuestion(int order, Long questionId, String questionStage,
                                                String questionType, String questionBody,
                                                List<OptionResponse> options) {
            return new BlockResponse(order, "QUESTION", null, null, null,
                    questionId, questionStage, questionType, questionBody, options);
        }
    }

    @Schema(description = "본문 블록 데이터")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record BodyBlockResponse(
            @Schema(description = "본문 제목", example = "월급 관리란?")
            String title,
            @Schema(description = "설명", example = "월급을 효율적으로 관리하는 방법입니다.")
            String description,
            @Schema(description = "추가 설명 (EXPLANATION 전용)")
            String additionalDescription,
            @Schema(description = "이미지 URL (CASE, COMPARISON 전용)")
            String imageUrl,
            @Schema(description = "표 이미지 URL (COMPARISON 전용)")
            String tableImageUrl
    ) {
    }

    @Schema(description = "선택지")
    public record OptionResponse(
            @Schema(description = "선택지 ID", example = "A")
            String optionId,
            @Schema(description = "선택지 텍스트", example = "소비를 줄인다")
            String optionText
    ) {
    }
}

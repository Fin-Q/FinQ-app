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
        @Schema(description = "콘텐츠 제목", example = "현금흐름")
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

    @Schema(description = "콘텐츠 블록. blockType에 따라 포함되는 필드가 다릅니다. "
            + "null인 필드는 JSON에서 생략됩니다(NON_NULL). "
            + "BODY → order, title, content 포함 / "
            + "QUESTION → questionId, questionStage, questionType, questionBody, options 포함")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record BlockResponse(
            @Schema(description = "페이지 순서 (BODY 블록 전용, QUESTION에서는 생략)", example = "1")
            Integer order,
            @Schema(description = "블록 유형", example = "BODY", allowableValues = {"BODY", "QUESTION"})
            String blockType,
            @Schema(description = "페이지 제목 (BODY 블록 전용)")
            String title,
            @Schema(description = "콘텐츠 항목 목록 (BODY 블록 전용)")
            List<ContentItemResponse> content,
            @Schema(description = "문제 ID (QUESTION 블록 전용)", example = "1")
            Long questionId,
            @Schema(description = "문제 단계 (QUESTION 블록 전용). "
                    + "P1=1차 확인 문제 / P2=2차 확인 문제 / F=최종 확인 문제",
                    example = "P1", allowableValues = {"P1", "P2", "F"})
            String questionStage,
            @Schema(description = "문제 유형 (QUESTION 블록 전용). "
                    + "OX=O/X 문제(선택지: O, X) / SINGLE_CHOICE=4지선다(선택지: A, B, C, D)",
                    example = "OX", allowableValues = {"OX", "SINGLE_CHOICE"})
            String questionType,
            @Schema(description = "문제 본문 (QUESTION 블록 전용)")
            String questionBody,
            @Schema(description = "보기 목록 (QUESTION 블록 전용). "
                    + "OX 문제: optionId는 O 또는 X / 4지선다: optionId는 A, B, C, D")
            List<OptionResponse> options
    ) {

        public static BlockResponse ofBody(int order, String title, List<ContentItemResponse> content) {
            return new BlockResponse(order, "BODY", title, content, null, null, null, null, null);
        }

        public static BlockResponse ofQuestion(Long questionId, String questionStage,
                                                String questionType, String questionBody,
                                                List<OptionResponse> options) {
            return new BlockResponse(null, "QUESTION", null, null,
                    questionId, questionStage, questionType, questionBody, options);
        }
    }

    @Schema(description = "콘텐츠 항목. type에 따라 포함되는 필드가 다릅니다. "
            + "null인 필드는 JSON에서 생략됩니다(NON_NULL). "
            + "TEXT → text / BOX → items / IMAGE → imageUrl / CAPTION → text")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ContentItemResponse(
            @Schema(description = "항목 유형", example = "TEXT", allowableValues = {"TEXT", "BOX", "IMAGE", "CAPTION"})
            String type,
            @Schema(description = "텍스트 (TEXT, CAPTION 전용). **볼드** 마크다운 지원, 줄바꿈은 \\n")
            String text,
            @Schema(description = "박스 항목 목록 (BOX 전용)")
            List<BoxItemResponse> items,
            @Schema(description = "이미지 URL (IMAGE 전용)")
            String imageUrl
    ) {
    }

    @Schema(description = "박스 항목")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record BoxItemResponse(
            @Schema(description = "박스 항목 제목 (선택)")
            String title,
            @Schema(description = "박스 항목 내용")
            String text
    ) {
    }

    @Schema(description = "선택지")
    public record OptionResponse(
            @Schema(description = "선택지 ID. OX 문제: 'O' 또는 'X' / 4지선다: 'A', 'B', 'C', 'D'", example = "A")
            String optionId,
            @Schema(description = "선택지 텍스트", example = "소비를 줄인다")
            String optionText
    ) {
    }
}

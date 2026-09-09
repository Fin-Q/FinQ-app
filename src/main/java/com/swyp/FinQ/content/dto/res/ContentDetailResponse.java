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

    @Schema(description = "콘텐츠 블록. blockType에 따라 포함되는 필드가 다릅니다. "
            + "null인 필드는 JSON에서 생략됩니다(NON_NULL). "
            + "BODY → bodyType, body 포함 / SUMMARY → summaryContent 포함 / "
            + "QUESTION → questionId, questionStage, questionType, questionBody, options 포함")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record BlockResponse(
            @Schema(description = "블록 순서", example = "1")
            int order,
            @Schema(description = "블록 유형", example = "BODY", allowableValues = {"BODY", "SUMMARY", "QUESTION"})
            String blockType,
            @Schema(description = "본문 유형 (BODY 블록 전용, 그 외 blockType에서는 생략). "
                    + "EXPLANATION=설명형 / CASE=사례형 / COMPARISON=비교형",
                    example = "EXPLANATION", allowableValues = {"EXPLANATION", "CASE", "COMPARISON"})
            String bodyType,
            @Schema(description = "본문 데이터 (BODY 블록 전용, 그 외 blockType에서는 생략)")
            BodyBlockResponse body,
            @Schema(description = "핵심 정리 내용 (SUMMARY 블록 전용, 그 외 blockType에서는 생략)")
            String summaryContent,
            @Schema(description = "문제 ID (QUESTION 블록 전용, 그 외 blockType에서는 생략)", example = "1")
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

    @Schema(description = "본문 블록 데이터. bodyType에 따라 포함되는 필드가 다릅니다. "
            + "null인 필드는 JSON에서 생략됩니다(NON_NULL). "
            + "EXPLANATION → title, description, additionalDescription / "
            + "CASE → title, description, imageUrl / "
            + "COMPARISON → title, description, imageUrl, tableImageUrl")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record BodyBlockResponse(
            @Schema(description = "본문 제목", example = "월급 관리란?")
            String title,
            @Schema(description = "설명", example = "월급을 효율적으로 관리하는 방법입니다.")
            String description,
            @Schema(description = "추가 설명 (EXPLANATION 전용, 그 외 bodyType에서는 생략)")
            String additionalDescription,
            @Schema(description = "사례/비교 이미지 URL (CASE, COMPARISON 전용, EXPLANATION에서는 생략). "
                    + "CASE=사례 관련 이미지 / COMPARISON=비교 관련 이미지. "
                    + "※ tableImageUrl과 구분: imageUrl은 본문 이미지, tableImageUrl은 '표' 전용 이미지. "
                    + "imageUrl 보유 콘텐츠: SAL-01(block 3), INV-02(block 3), INV-03(block 3), ETF-01(block 3)")
            String imageUrl,
            @Schema(description = "표 이미지 URL (COMPARISON 전용, 그 외 bodyType에서는 생략). "
                    + "비교형에서 '표'로 분리된 이미지. imageUrl(본문 이미지)과 별도로 존재. "
                    + "tableImageUrl 보유 콘텐츠: SAL-04(block 3), TAX-09(block 3)")
            String tableImageUrl
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

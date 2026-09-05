package com.swyp.FinQ.learning.dto.res;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.swyp.FinQ.learning.domain.NextAction;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "콘텐츠 문제 채점 응답")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ContentAnswerResponse(
        @Schema(description = "정답 여부", example = "true")
        boolean correct,
        @Schema(description = "해설", example = "복리는 이자에 이자가 붙는 방식입니다.")
        String explanation,
        @Schema(description = "사용자가 선택한 보기 ID", example = "A")
        String selectedOptionId,
        @Schema(description = "정답 보기 ID", example = "A")
        String correctOptionId,
        @Schema(description = "다음 행동 지시", example = "NEXT_BODY",
                allowableValues = {"NEXT_BODY", "NEXT_SUMMARY", "NEXT_QUESTION", "CONTENT_COMPLETED", "RETRY"})
        String nextAction,
        @Schema(description = "콘텐츠 최초 완료 시 결과 (미완료 또는 이미 완료된 경우 null)")
        ContentResult contentResult
) {

    @Schema(description = "콘텐츠 완료 결과")
    public record ContentResult(
            @Schema(description = "획득 XP", example = "10")
            int earnedXp,
            @Schema(description = "레벨업 여부", example = "false")
            boolean levelUp,
            @Schema(description = "레벨업 시 새 레벨 (레벨업하지 않은 경우 null)", example = "2")
            Integer newLevel
    ) {
    }

    public static ContentAnswerResponse correct(String explanation, String selectedOptionId,
                                                 String correctOptionId, String nextAction,
                                                 ContentResult contentResult) {
        return new ContentAnswerResponse(true, explanation, selectedOptionId, correctOptionId,
                nextAction, contentResult);
    }

    public static ContentAnswerResponse incorrect(String explanation, String selectedOptionId,
                                                   String correctOptionId) {
        return new ContentAnswerResponse(false, explanation, selectedOptionId, correctOptionId,
                NextAction.RETRY.name(), null);
    }
}
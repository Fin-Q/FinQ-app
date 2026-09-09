package com.swyp.FinQ.learning.dto.res;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.swyp.FinQ.learning.domain.NextAction;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "콘텐츠 문제 채점 응답. 이미 완료한 콘텐츠도 재풀이 가능하며, 채점/해설은 동일하게 제공됩니다. "
        + "단, XP는 최초 완료 시에만 지급됩니다.")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ContentAnswerResponse(
        @Schema(description = "정답 여부", example = "true")
        boolean correct,
        @Schema(description = "해설", example = "복리는 이자에 이자가 붙는 방식입니다.")
        String explanation,
        @Schema(description = "사용자가 선택한 보기 ID. OX 문제: 'O' 또는 'X' / 4지선다: 'A', 'B', 'C', 'D'", example = "A")
        String selectedOptionId,
        @Schema(description = "정답 보기 ID", example = "A")
        String correctOptionId,
        @Schema(description = "다음 행동 지시. "
                + "NEXT_BODY=다음 본문 블록으로 이동 / NEXT_SUMMARY=핵심 정리 블록으로 이동 / "
                + "NEXT_QUESTION=다음 문제 블록으로 이동 / CONTENT_COMPLETED=콘텐츠 학습 완료 / "
                + "RETRY=오답, 같은 문제 재시도",
                example = "NEXT_BODY",
                allowableValues = {"NEXT_BODY", "NEXT_SUMMARY", "NEXT_QUESTION", "CONTENT_COMPLETED", "RETRY"})
        String nextAction,
        @JsonInclude(JsonInclude.Include.ALWAYS)
        @Schema(description = "콘텐츠 최초 완료 시 보상 결과. "
                + "F단계 정답이면서 해당 콘텐츠를 처음 완료한 경우에만 값이 존재합니다. "
                + "오답이거나 이미 완료한 콘텐츠 재풀이 시에는 null (채점/해설은 정상 제공, XP 미지급). "
                + "null이어도 JSON에 항상 포함됩니다.",
                nullable = true)
        ContentResult contentResult
) {

    @Schema(description = "콘텐츠 완료 보상 결과 (스트릭 보너스 XP 포함)")
    public record ContentResult(
            @Schema(description = "획득 XP (콘텐츠 완료 XP + 스트릭 보너스 XP 합산)", example = "10")
            int earnedXp,
            @Schema(description = "레벨업 여부", example = "false")
            boolean levelUp,
            @Schema(description = "레벨업 시 새 레벨 값 (레벨업하지 않은 경우 null). "
                    + "레벨 체계: LV1(0xp) → LV2(80xp) → LV3(180xp) → LV4(300xp)",
                    example = "2")
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

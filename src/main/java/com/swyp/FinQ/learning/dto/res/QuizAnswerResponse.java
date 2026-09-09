package com.swyp.FinQ.learning.dto.res;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "심화퀴즈 채점 응답. 이미 완료한 카테고리도 재풀이 가능하며, 채점/해설은 동일하게 제공됩니다. "
        + "단, XP는 최초 완료 시에만 지급됩니다. 심화퀴즈는 항상 3문제입니다.")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record QuizAnswerResponse(
        @Schema(description = "정답 여부", example = "true")
        boolean correct,
        @Schema(description = "해설", example = "복리는 원금뿐 아니라 이자에도 이자가 붙습니다.")
        String explanation,
        @Schema(description = "사용자가 선택한 보기 ID. 심화퀴즈는 모두 4지선다이므로 'A', 'B', 'C', 'D' 중 하나", example = "A")
        String selectedOptionId,
        @Schema(description = "정답 보기 ID", example = "A")
        String correctOptionId,
        @Schema(description = "마지막 문제(3번째) 여부. true이면 퀴즈 완료 화면으로 전환", example = "false")
        boolean isLastQuestion,
        @JsonInclude(JsonInclude.Include.ALWAYS)
        @Schema(description = "카테고리 최초 완료 시 보상 결과. "
                + "마지막 문제(isLastQuestion=true) 정답이면서 해당 카테고리를 처음 완료한 경우에만 값이 존재합니다. "
                + "오답이거나 이미 완료한 카테고리 재풀이 시에는 null (채점/해설은 정상 제공, XP 미지급). "
                + "null이어도 JSON에 항상 포함됩니다.",
                nullable = true)
        CategoryResult categoryResult
) {

    @Schema(description = "카테고리 완료 보상 결과 (스트릭 보너스 XP 포함)")
    public record CategoryResult(
            @Schema(description = "획득 XP (퀴즈 완료 XP + 스트릭 보너스 XP 합산)", example = "30")
            int earnedXp,
            @Schema(description = "레벨업 여부", example = "false")
            boolean levelUp,
            @Schema(description = "레벨업 시 새 레벨 값 (레벨업하지 않은 경우 null). "
                    + "레벨 체계: LV1(0xp) → LV2(80xp) → LV3(180xp) → LV4(300xp)",
                    example = "3")
            Integer newLevel
    ) {
    }

    public static QuizAnswerResponse correct(String explanation, String selectedOptionId,
                                              String correctOptionId, boolean isLastQuestion,
                                              CategoryResult categoryResult) {
        return new QuizAnswerResponse(true, explanation, selectedOptionId, correctOptionId,
                isLastQuestion, categoryResult);
    }

    public static QuizAnswerResponse incorrect(String explanation, String selectedOptionId,
                                                String correctOptionId, boolean isLastQuestion) {
        return new QuizAnswerResponse(false, explanation, selectedOptionId, correctOptionId,
                isLastQuestion, null);
    }
}

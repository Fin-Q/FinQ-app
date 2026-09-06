package com.swyp.FinQ.learning.dto.res;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "심화퀴즈 채점 응답")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record QuizAnswerResponse(
        @Schema(description = "정답 여부", example = "true")
        boolean correct,
        @Schema(description = "해설", example = "복리는 원금뿐 아니라 이자에도 이자가 붙습니다.")
        String explanation,
        @Schema(description = "사용자가 선택한 보기 ID", example = "A")
        String selectedOptionId,
        @Schema(description = "정답 보기 ID", example = "A")
        String correctOptionId,
        @Schema(description = "마지막 문제 여부", example = "false")
        boolean isLastQuestion,
        @Schema(description = "카테고리 최초 완료 시 결과 (미완료 또는 이미 완료된 경우 null)")
        CategoryResult categoryResult
) {

    @Schema(description = "카테고리 완료 결과")
    public record CategoryResult(
            @Schema(description = "획득 XP", example = "30")
            int earnedXp,
            @Schema(description = "레벨업 여부", example = "false")
            boolean levelUp,
            @Schema(description = "레벨업 시 새 레벨 (레벨업하지 않은 경우 null)", example = "3")
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
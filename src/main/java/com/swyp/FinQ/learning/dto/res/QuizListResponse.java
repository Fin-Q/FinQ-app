package com.swyp.FinQ.learning.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "심화퀴즈 목록 조회 응답")
public record QuizListResponse(
        @Schema(description = "카테고리 ID", example = "1")
        Long categoryId,
        @Schema(description = "카테고리명", example = "월급 관리")
        String categoryName,
        @Schema(description = "퀴즈 완료 시 획득 XP", example = "30")
        int rewardXp,
        @Schema(description = "퀴즈 문제 목록")
        List<QuizQuestion> questions
) {

    @Schema(description = "심화퀴즈 문제")
    public record QuizQuestion(
            @Schema(description = "문제 ID", example = "1")
            Long questionId,
            @Schema(description = "문제 순서", example = "1")
            int order,
            @Schema(description = "문제 유형", example = "SINGLE_CHOICE")
            String questionType,
            @Schema(description = "문제 본문", example = "다음 중 복리의 특징으로 올바른 것은?")
            String questionBody,
            @Schema(description = "보기 목록")
            List<Option> options
    ) {
    }

    @Schema(description = "선택지")
    public record Option(
            @Schema(description = "선택지 ID", example = "A")
            String optionId,
            @Schema(description = "선택지 텍스트", example = "이자에 이자가 붙는다")
            String optionText
    ) {
    }
}
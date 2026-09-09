package com.swyp.FinQ.learning.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "심화퀴즈 목록 조회 응답. 심화퀴즈는 항상 3문제이며, 모두 4지선다(SINGLE_CHOICE)입니다.")
public record QuizListResponse(
        @Schema(description = "카테고리 ID", example = "1")
        Long categoryId,
        @Schema(description = "카테고리명", example = "월급 관리")
        String categoryName,
        @Schema(description = "퀴즈 최초 완료 시 획득 XP (이미 완료한 카테고리 재풀이 시에는 지급되지 않음)", example = "30")
        int rewardXp,
        @Schema(description = "심화퀴즈 안내 제목 (퀴즈 시작 전 안내 화면용)", example = "월급관리·저축, 얼마나 이해했을까요?")
        String introTitle,
        @Schema(description = "심화퀴즈 안내 설명 (퀴즈 시작 전 안내 화면용)")
        String introDescription,
        @Schema(description = "심화퀴즈 완료 제목 (퀴즈 완료 후 결과 화면용)", example = "월급관리·저축 심화퀴즈 완료!")
        String completionTitle,
        @Schema(description = "심화퀴즈 완료 설명 (퀴즈 완료 후 결과 화면용)")
        String completionDescription,
        @Schema(description = "퀴즈 문제 목록 (항상 3문제)")
        List<QuizQuestion> questions
) {

    @Schema(description = "심화퀴즈 문제")
    public record QuizQuestion(
            @Schema(description = "문제 ID", example = "1")
            Long questionId,
            @Schema(description = "문제 순서 (1~3)", example = "1")
            int order,
            @Schema(description = "문제 유형. 심화퀴즈는 모두 SINGLE_CHOICE(4지선다)",
                    example = "SINGLE_CHOICE", allowableValues = {"SINGLE_CHOICE"})
            String questionType,
            @Schema(description = "문제 본문", example = "다음 중 복리의 특징으로 올바른 것은?")
            String questionBody,
            @Schema(description = "보기 목록 (A, B, C, D 4개)")
            List<Option> options
    ) {
    }

    @Schema(description = "선택지")
    public record Option(
            @Schema(description = "선택지 ID (A, B, C, D)", example = "A",
                    allowableValues = {"A", "B", "C", "D"})
            String optionId,
            @Schema(description = "선택지 텍스트", example = "이자에 이자가 붙는다")
            String optionText
    ) {
    }
}
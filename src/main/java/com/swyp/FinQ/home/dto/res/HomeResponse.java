package com.swyp.FinQ.home.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "홈 화면 조회 응답")
public record HomeResponse(
        @Schema(description = "사용자 닉네임", example = "핀큐")
        String nickname,
        @Schema(description = "현재 레벨", example = "2")
        int level,
        @Schema(description = "캐릭터 성장 단계", example = "2")
        int characterStage,
        @Schema(description = "누적 경험치", example = "120")
        int totalXp,
        @Schema(description = "현재 연속 학습 일수", example = "3")
        int currentStreak,
        @Schema(description = "추천 질문 카드 목록")
        List<QuestionCard> questions
) {

    @Schema(description = "추천 질문 카드")
    public record QuestionCard(
            @Schema(description = "콘텐츠 ID", example = "1")
            String contentId,
            @Schema(description = "카테고리 코드", example = "SAL")
            String categoryCode,
            @Schema(description = "카테고리명", example = "월급 관리")
            String categoryName,
            @Schema(description = "콘텐츠 제목", example = "월급을 받으면 가장 먼저 해야 할 일은?")
            String title,
            @Schema(description = "완료 상태 (COMPLETED / INCOMPLETE)", example = "INCOMPLETE")
            String completionStatus
    ) {
    }
}
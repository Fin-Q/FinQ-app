package com.swyp.FinQ.learning.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "답안 제출 요청. 콘텐츠 문제와 심화퀴즈 모두 이 요청을 사용합니다.")
public record AnswerRequest(
        @Schema(description = "선택한 보기 ID. "
                + "OX 문제: 'O' 또는 'X' / 4지선다(SINGLE_CHOICE): 'A', 'B', 'C', 'D'",
                example = "A")
        @NotBlank(message = "선택한 보기 ID는 필수입니다.")
        String selectedOptionId
) {
}
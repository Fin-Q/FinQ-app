package com.swyp.FinQ.learning.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "답안 제출 요청")
public record AnswerRequest(
        @Schema(description = "선택한 보기 ID", example = "A")
        @NotBlank(message = "선택한 보기 ID는 필수입니다.")
        String selectedOptionId
) {
}
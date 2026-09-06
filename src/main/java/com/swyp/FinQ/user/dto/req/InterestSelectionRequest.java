package com.swyp.FinQ.user.dto.req;

import com.swyp.FinQ.content.domain.CategoryCode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "관심 주제 선택 요청")
public record InterestSelectionRequest(
        @Schema(description = "관심 카테고리 코드", example = "[\"SAL\", \"INV\"]")
        @NotEmpty(message = "관심 주제를 한 개 이상 선택해야 합니다")
        @Size(max = 4, message = "관심 주제는 최대 4개까지 선택할 수 있습니다") // NOTE: - 관심주제 최대 개수 꼭 확인
        List<@NotNull(message = "관심 주제는 null일 수 없습니다") CategoryCode> categoryCodes
) {
}

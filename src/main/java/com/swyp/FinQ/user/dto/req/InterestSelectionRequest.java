package com.swyp.FinQ.user.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "관심 주제 선택 요청")
public record InterestSelectionRequest(
        @Schema(description = "관심 주제 ID. 1~2개이며 중복과 null은 허용하지 않음",
                example = "[1, 2]")
        @NotEmpty(message = "관심 주제를 한 개 이상 선택해야 합니다")
        @Size(max = 2, message = "관심 주제는 최대 2개까지 선택할 수 있습니다")
        List<@NotNull(message = "관심 주제는 null일 수 없습니다") Long> interestTopicIds
) {
}

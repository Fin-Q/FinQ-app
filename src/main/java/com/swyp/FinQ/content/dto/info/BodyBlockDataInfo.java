package com.swyp.FinQ.content.dto.info;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "본문 블록 데이터")
@JsonIgnoreProperties(ignoreUnknown = true)
public record BodyBlockDataInfo(
        @Schema(description = "본문 타입", example = "TEXT")
        String bodyType,
        @Schema(description = "블록 순서", example = "1")
        int order,
        @Schema(description = "블록 제목", example = "월급 관리의 기본")
        String title,
        @Schema(description = "블록 설명")
        String description,
        @Schema(description = "추가 설명")
        String additionalDescription,
        @Schema(description = "이미지 URL")
        String imageUrl,
        @Schema(description = "표 이미지 URL")
        String tableImageUrl
) {
}
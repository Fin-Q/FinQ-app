package com.swyp.FinQ.content.dto.info;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "본문 페이지 데이터 (body_data JSON 구조)")
@JsonIgnoreProperties(ignoreUnknown = true)
public record BodyBlockDataInfo(
        @Schema(description = "페이지 순서", example = "1")
        int order,
        @Schema(description = "페이지 제목", example = "도입")
        String title,
        @Schema(description = "콘텐츠 항목 목록")
        List<ContentItem> content
) {

    @Schema(description = "콘텐츠 항목. type에 따라 사용되는 필드가 다릅니다. "
            + "TEXT → text / BOX → items / IMAGE → imageUrl / CAPTION → text")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ContentItem(
            @Schema(description = "항목 유형", example = "TEXT",
                    allowableValues = {"TEXT", "BOX", "IMAGE", "CAPTION"})
            String type,
            @Schema(description = "텍스트 내용 (TEXT, CAPTION 전용). **볼드** 마크다운 지원, 줄바꿈은 \\n")
            String text,
            @Schema(description = "박스 항목 목록 (BOX 전용)")
            List<BoxItem> items,
            @Schema(description = "이미지 URL (IMAGE 전용)")
            String imageUrl
    ) {
    }

    @Schema(description = "박스 내부 항목")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record BoxItem(
            @Schema(description = "항목 제목 (선택, nullable)")
            String title,
            @Schema(description = "항목 내용")
            String text
    ) {
    }
}
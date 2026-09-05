package com.swyp.FinQ.content.dto.info;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BodyBlockDataInfo(
        String bodyType,
        int order,
        String title,
        String description,
        String additionalDescription,
        String imageUrl,
        String tableImageUrl
) {
}
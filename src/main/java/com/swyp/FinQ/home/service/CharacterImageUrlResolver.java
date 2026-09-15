package com.swyp.FinQ.home.service;

import com.swyp.FinQ.home.config.CharacterImageProperties;
import com.swyp.FinQ.reward.domain.Level;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class CharacterImageUrlResolver {

    private static final Map<Level, String> FILE_NAMES = Map.of(
            Level.LV1, "character_01.png",
            Level.LV2, "character_02.png",
            Level.LV3, "character_03.png",
            Level.LV4, "character_04.png"
    );

    private final CharacterImageProperties properties;

    public String resolve(Level level) {
        Objects.requireNonNull(level, "Level must not be null");
        return properties.baseUrl() + "/" + FILE_NAMES.get(level);
    }
}

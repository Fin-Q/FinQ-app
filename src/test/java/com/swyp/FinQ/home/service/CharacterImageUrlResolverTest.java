package com.swyp.FinQ.home.service;

import com.swyp.FinQ.home.config.CharacterImageProperties;
import com.swyp.FinQ.reward.domain.Level;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class CharacterImageUrlResolverTest {

    private static final String BASE_URL =
            "https://assets.example.com/character-images";

    @ParameterizedTest
    @CsvSource({
            "LV1, character_01.png",
            "LV2, character_02.png",
            "LV3, character_03.png",
            "LV4, character_04.png"
    })
    void resolvesImageFileNameByLevel(Level level, String fileName) {
        CharacterImageUrlResolver resolver = new CharacterImageUrlResolver(
                new CharacterImageProperties(BASE_URL)
        );

        assertThat(resolver.resolve(level)).isEqualTo(BASE_URL + "/" + fileName);
    }

    @Test
    void removesTrailingSlashes() {
        CharacterImageUrlResolver resolver = new CharacterImageUrlResolver(
                new CharacterImageProperties(BASE_URL + "///")
        );

        assertThat(resolver.resolve(Level.LV1))
                .isEqualTo(BASE_URL + "/character_01.png");
    }

    @Test
    void supportsCdnBaseUrlWithoutChangingLevelMapping() {
        CharacterImageUrlResolver resolver = new CharacterImageUrlResolver(
                new CharacterImageProperties("https://cdn.example.com/characters")
        );

        assertThat(resolver.resolve(Level.LV4))
                .isEqualTo("https://cdn.example.com/characters/character_04.png");
    }

    @Test
    void rejectsNullLevel() {
        CharacterImageUrlResolver resolver = new CharacterImageUrlResolver(
                new CharacterImageProperties(BASE_URL)
        );

        assertThatNullPointerException().isThrownBy(() -> resolver.resolve(null));
    }
}

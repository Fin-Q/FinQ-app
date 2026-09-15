package com.swyp.FinQ.home.config;

import com.swyp.FinQ.home.service.CharacterImageUrlResolver;
import com.swyp.FinQ.reward.domain.Level;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class CharacterImageConfigTest {

    @Test
    void bindsPropertiesAndCreatesResolver() {
        new ApplicationContextRunner()
                .withUserConfiguration(CharacterImageConfig.class, CharacterImageUrlResolver.class)
                .withPropertyValues("character-image.base-url=https://cdn.example.com/characters/")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).hasSingleBean(CharacterImageProperties.class);
                    assertThat(context.getBean(CharacterImageUrlResolver.class).resolve(Level.LV1))
                            .isEqualTo("https://cdn.example.com/characters/character_01.png");
                });
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
            " ", "http://example.com/characters", "characters", "https:///characters",
            "https://user:password@example.com/characters", "https://example.com/characters?token=123",
            "https://example.com/characters#section", "https://example.com/a b"
    })
    void rejectsInvalidBaseUrls(String baseUrl) {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new CharacterImageProperties(baseUrl));
    }

    @Test
    void failsContextStartupWhenBaseUrlIsMissing() {
        new ApplicationContextRunner()
                .withUserConfiguration(CharacterImageConfig.class)
                .run(context -> assertThat(context).hasFailed());
    }

    @Test
    void failsContextStartupWhenBaseUrlIsInvalid() {
        new ApplicationContextRunner()
                .withUserConfiguration(CharacterImageConfig.class)
                .withPropertyValues("character-image.base-url=http://example.com/characters")
                .run(context -> assertThat(context).hasFailed());
    }
}

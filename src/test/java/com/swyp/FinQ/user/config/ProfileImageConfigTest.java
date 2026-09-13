package com.swyp.FinQ.user.config;

import com.swyp.FinQ.user.domain.ProfileImageCode;
import com.swyp.FinQ.user.service.ProfileImageUrlResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class ProfileImageConfigTest {

    @Test
    void bindsPropertiesAndCreatesResolver() {
        new ApplicationContextRunner()
                .withUserConfiguration(ProfileImageConfig.class, ProfileImageUrlResolver.class)
                .withPropertyValues("profile-image.base-url=https://cdn.example.com/profiles/")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).hasSingleBean(ProfileImageProperties.class);
                    assertThat(context.getBean(ProfileImageUrlResolver.class)
                            .resolve(ProfileImageCode.PROFILE_01))
                            .isEqualTo("https://cdn.example.com/profiles/profile_01.png");
                });
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
            " ", "http://example.com/profiles", "profiles", "https:///profiles",
            "https://user:password@example.com/profiles", "https://example.com/profiles?token=123",
            "https://example.com/profiles#section", "https://example.com/a b"
    })
    void rejectsInvalidBaseUrls(String baseUrl) {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new ProfileImageProperties(baseUrl));
    }

    @Test
    void failsContextStartupWhenBaseUrlIsMissing() {
        new ApplicationContextRunner()
                .withUserConfiguration(ProfileImageConfig.class)
                .run(context -> assertThat(context).hasFailed());
    }

    @Test
    void failsContextStartupWhenBaseUrlIsInvalid() {
        new ApplicationContextRunner()
                .withUserConfiguration(ProfileImageConfig.class)
                .withPropertyValues("profile-image.base-url=http://example.com/profiles")
                .run(context -> assertThat(context).hasFailed());
    }
}

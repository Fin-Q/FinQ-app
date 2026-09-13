package com.swyp.FinQ.user.service;

import com.swyp.FinQ.user.config.ProfileImageProperties;
import com.swyp.FinQ.user.domain.ProfileImageCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class ProfileImageUrlResolverTest {

    private static final String BASE_URL =
            "https://assets.example.com/profile-images";

    @ParameterizedTest
    @CsvSource({
            "PROFILE_01, profile_01.png",
            "PROFILE_02, profile_02.png",
            "PROFILE_03, profile_03.png",
            "PROFILE_04, profile_04.png"
    })
    void resolvesUploadedFileNames(ProfileImageCode code, String fileName) {
        ProfileImageUrlResolver resolver = new ProfileImageUrlResolver(
                new ProfileImageProperties(BASE_URL)
        );

        assertThat(resolver.resolve(code)).isEqualTo(BASE_URL + "/" + fileName);
    }

    @Test
    void removesTrailingSlashes() {
        ProfileImageUrlResolver resolver = new ProfileImageUrlResolver(
                new ProfileImageProperties(BASE_URL + "///")
        );

        assertThat(resolver.resolve(ProfileImageCode.PROFILE_01))
                .isEqualTo(BASE_URL + "/profile_01.png");
    }

    @Test
    void supportsCdnBaseUrlWithoutChangingImageCodes() {
        ProfileImageUrlResolver resolver = new ProfileImageUrlResolver(
                new ProfileImageProperties("https://cdn.example.com/profiles")
        );

        assertThat(resolver.resolve(ProfileImageCode.PROFILE_04))
                .isEqualTo("https://cdn.example.com/profiles/profile_04.png");
    }

    @Test
    void rejectsNullImageCode() {
        ProfileImageUrlResolver resolver = new ProfileImageUrlResolver(
                new ProfileImageProperties(BASE_URL)
        );

        assertThatNullPointerException().isThrownBy(() -> resolver.resolve(null));
    }
}

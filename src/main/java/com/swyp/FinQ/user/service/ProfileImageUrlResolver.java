package com.swyp.FinQ.user.service;

import com.swyp.FinQ.user.config.ProfileImageProperties;
import com.swyp.FinQ.user.domain.ProfileImageCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class ProfileImageUrlResolver {

    private final ProfileImageProperties properties;

    public String resolve(ProfileImageCode code) {
        Objects.requireNonNull(code, "Profile image code must not be null");
        return properties.baseUrl() + "/" + code.getFileName();
    }
}

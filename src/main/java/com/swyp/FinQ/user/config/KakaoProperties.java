package com.swyp.FinQ.user.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;

@ConfigurationProperties(prefix = "kakao")
public record KakaoProperties(
        String appId,
        URI apiBaseUrl
) {
}

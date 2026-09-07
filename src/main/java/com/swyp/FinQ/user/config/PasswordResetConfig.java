package com.swyp.FinQ.user.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sesv2.SesV2Client;

@Configuration
@EnableConfigurationProperties({PasswordResetProperties.class, SesMailProperties.class})
public class PasswordResetConfig {

    @Bean
    public SesV2Client sesV2Client(SesMailProperties properties) {
        return SesV2Client.builder()
                .region(Region.of(properties.region()))
                .build();
    }
}

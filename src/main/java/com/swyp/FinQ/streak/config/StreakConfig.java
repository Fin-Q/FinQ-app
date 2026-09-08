package com.swyp.FinQ.streak.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class StreakConfig {

    public static final ZoneId STREAK_ZONE_ID = ZoneId.of("Asia/Seoul");

    @Bean
    public Clock streakClock() {
        return Clock.system(STREAK_ZONE_ID);
    }
}

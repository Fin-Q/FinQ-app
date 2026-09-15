package com.swyp.FinQ.streak.config;

import com.swyp.FinQ.streak.service.StreakStatisticsScheduler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration(proxyBeanMethods = false)
@EnableScheduling
@Import(StreakStatisticsScheduler.class)
@ConditionalOnProperty(name = "streak.statistics.enabled", havingValue = "true")
public class StreakStatisticsSchedulingConfig {
}

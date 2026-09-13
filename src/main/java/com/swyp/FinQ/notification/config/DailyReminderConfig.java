package com.swyp.FinQ.notification.config;

import com.swyp.FinQ.notification.service.DailyLearningReminderScheduler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration(proxyBeanMethods = false)
@EnableScheduling
@Import(DailyLearningReminderScheduler.class)
@ConditionalOnProperty(name = {"firebase.enabled", "notification.daily-reminder.enabled"}, havingValue = "true")
public class DailyReminderConfig {
}

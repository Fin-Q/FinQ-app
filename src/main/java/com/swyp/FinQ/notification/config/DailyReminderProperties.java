package com.swyp.FinQ.notification.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties(prefix = "notification.daily-reminder")
public record DailyReminderProperties(Map<String, String> data) {

    public DailyReminderProperties {
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("Daily reminder routing data must be configured before enabling scheduling");
        }
        if (data.entrySet().stream().anyMatch(entry -> entry.getKey() == null || entry.getKey().isBlank()
                || entry.getValue() == null || entry.getValue().isBlank())) {
            throw new IllegalArgumentException("Daily reminder routing data must not contain blank keys or values");
        }
        data = Map.copyOf(data);
    }
}

package com.swyp.FinQ.streak.config;

import com.swyp.FinQ.notification.config.DailyReminderConfig;
import com.swyp.FinQ.notification.service.PushNotificationService;
import com.swyp.FinQ.streak.service.StreakStatisticsScheduler;
import com.swyp.FinQ.streak.service.StreakStatisticsService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;

import java.time.Clock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class StreakStatisticsSchedulingConfigTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(StreakStatisticsSchedulingConfig.class)
            .withBean(StreakStatisticsService.class, () -> mock(StreakStatisticsService.class))
            .withBean(Clock.class, Clock::systemUTC);

    @Test
    void registersStatisticsTaskWithoutFirebaseOrReminder() {
        runner.withPropertyValues("streak.statistics.enabled=true", "firebase.enabled=false")
                .run(context -> {
                    assertThat(context).hasNotFailed().hasSingleBean(StreakStatisticsScheduler.class);
                    assertThat(context.getBean(ScheduledAnnotationBeanPostProcessor.class).getScheduledTasks())
                            .hasSize(1);
                });
    }

    @Test
    void doesNotRegisterStatisticsTaskByDefaultOrWhenDisabled() {
        for (String[] settings : new String[][]{{}, {"streak.statistics.enabled=false"}}) {
            runner.withPropertyValues(settings).run(context -> {
                assertThat(context).hasNotFailed().doesNotHaveBean(StreakStatisticsScheduler.class)
                        .doesNotHaveBean(ScheduledAnnotationBeanPostProcessor.class);
            });
        }
    }

    @Test
    void registersBothCronTasksWithoutDuplicatingSchedulingInfrastructure() {
        runner.withUserConfiguration(DailyReminderConfig.class)
                .withBean(PushNotificationService.class, () -> mock(PushNotificationService.class))
                .withPropertyValues("streak.statistics.enabled=true", "firebase.enabled=true",
                        "notification.daily-reminder.enabled=true")
                .run(context -> {
                    assertThat(context).hasNotFailed().hasSingleBean(ScheduledAnnotationBeanPostProcessor.class);
                    assertThat(context.getBean(ScheduledAnnotationBeanPostProcessor.class).getScheduledTasks())
                            .hasSize(2);
                });
    }
}

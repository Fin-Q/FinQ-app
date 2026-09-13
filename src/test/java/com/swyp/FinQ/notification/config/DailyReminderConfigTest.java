package com.swyp.FinQ.notification.config;

import com.swyp.FinQ.notification.service.DailyLearningReminderScheduler;
import com.swyp.FinQ.notification.service.PushNotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class DailyReminderConfigTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(DailyReminderConfig.class)
            .withBean(PushNotificationService.class, () -> mock(PushNotificationService.class));

    @Test
    void registersExactlyOneCronTaskWhenBothFlagsAndRoutingDataAreConfigured() {
        runner.withPropertyValues("firebase.enabled=true", "notification.daily-reminder.enabled=true",
                "notification.daily-reminder.data.screen=HOME").run(context -> {
            assertThat(context).hasNotFailed().hasSingleBean(DailyLearningReminderScheduler.class);
            assertThat(context.getBean(ScheduledAnnotationBeanPostProcessor.class).getScheduledTasks()).hasSize(1);
            assertThat(context.getBean(DailyReminderProperties.class).data()).containsEntry("screen", "HOME");
        });
    }

    @Test
    void doesNotRegisterSchedulingByDefaultOrWhenEitherFlagIsDisabled() {
        for (String[] settings : new String[][]{
                {}, {"firebase.enabled=true"},
                {"firebase.enabled=false", "notification.daily-reminder.enabled=true"},
                {"firebase.enabled=true", "notification.daily-reminder.enabled=false"}}) {
            runner.withPropertyValues(settings).run(context -> {
                assertThat(context).hasNotFailed().doesNotHaveBean(DailyLearningReminderScheduler.class)
                        .doesNotHaveBean(ScheduledAnnotationBeanPostProcessor.class);
            });
        }
    }

    @Test
    void refusesActivationWithoutRoutingData() {
        runner.withPropertyValues("firebase.enabled=true", "notification.daily-reminder.enabled=true")
                .run(context -> assertThat(context).hasFailed());
    }

    @Test
    void refusesActivationWithBlankRoutingValue() {
        runner.withPropertyValues("firebase.enabled=true", "notification.daily-reminder.enabled=true",
                        "notification.daily-reminder.data.screen= ")
                .run(context -> assertThat(context).hasFailed());
    }
}

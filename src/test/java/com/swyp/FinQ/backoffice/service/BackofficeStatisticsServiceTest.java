package com.swyp.FinQ.backoffice.service;

import com.swyp.FinQ.backoffice.dto.BackofficeStatisticsResponse;
import com.swyp.FinQ.backoffice.repository.BackofficeStatisticsQueryRepository;
import com.swyp.FinQ.global.exception.BaseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BackofficeStatisticsServiceTest {

    @Mock
    private BackofficeStatisticsQueryRepository repository;

    private BackofficeStatisticsService service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(
                Instant.parse("2026-10-02T00:00:00Z"),
                ZoneId.of("Asia/Seoul")
        );
        service = new BackofficeStatisticsService(repository, clock);
    }

    @Test
    void returnsDashboardSourceForInclusiveDateRange() {
        LocalDate from = LocalDate.of(2026, 9, 1);
        LocalDate to = LocalDate.of(2026, 9, 30);
        when(repository.countUsersBefore(from.atStartOfDay())).thenReturn(120L);
        when(repository.findDailyStatistics(from, to)).thenReturn(List.of());
        when(repository.findDailySignups(from.atStartOfDay(), to.plusDays(1).atStartOfDay()))
                .thenReturn(List.of(new BackofficeStatisticsResponse.Signup(from, 3L)));

        BackofficeStatisticsResponse response = service.getStatistics(from, to);

        assertThat(response.usersBeforeRange()).isEqualTo(120L);
        assertThat(response.signups()).hasSize(1);
        verify(repository).findDailySignups(
                from.atStartOfDay(),
                to.plusDays(1).atStartOfDay()
        );
    }

    @Test
    void rejectsFutureOrOversizedDateRange() {
        assertThatThrownBy(() -> service.getStatistics(
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 3)
        )).isInstanceOf(BaseException.class);

        assertThatThrownBy(() -> service.getStatistics(
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2026, 1, 2)
        )).isInstanceOf(BaseException.class);
    }
}

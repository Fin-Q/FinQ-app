package com.swyp.FinQ.backoffice.controller;

import com.swyp.FinQ.backoffice.dto.BackofficeStatisticsResponse;
import com.swyp.FinQ.backoffice.service.BackofficeStatisticsService;
import com.swyp.FinQ.support.MySqlContainerSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "backoffice.api-key=test-backoffice-api-key-32-characters")
class BackofficeStatisticsControllerTest extends MySqlContainerSupport {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BackofficeStatisticsService statisticsService;

    @Test
    void returnsStatisticsWithValidBackofficeApiKey() throws Exception {
        LocalDate from = LocalDate.of(2026, 9, 1);
        LocalDate to = LocalDate.of(2026, 9, 2);
        when(statisticsService.getStatistics(from, to)).thenReturn(
                new BackofficeStatisticsResponse(
                        100L,
                        List.of(new BackofficeStatisticsResponse.Statistic(
                                to, 80L, 25L, 9L,
                                LocalDateTime.of(2026, 9, 3, 0, 5)
                        )),
                        List.of(new BackofficeStatisticsResponse.Signup(from, 4L))
                )
        );

        mockMvc.perform(get("/internal/backoffice/statistics")
                        .queryParam("from", from.toString())
                        .queryParam("to", to.toString())
                        .header("X-Backoffice-Api-Key", "test-backoffice-api-key-32-characters"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.usersBeforeRange").value(100))
                .andExpect(jsonPath("$.data.statistics[0].learnedUsers").value(80))
                .andExpect(jsonPath("$.data.signups[0].count").value(4));
    }

    @Test
    void rejectsMissingOrInvalidBackofficeApiKey() throws Exception {
        mockMvc.perform(get("/internal/backoffice/statistics")
                        .queryParam("from", "2026-09-01")
                        .queryParam("to", "2026-09-02"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/internal/backoffice/statistics")
                        .queryParam("from", "2026-09-01")
                        .queryParam("to", "2026-09-02")
                        .header("X-Backoffice-Api-Key", "wrong-key"))
                .andExpect(status().isUnauthorized());
    }
}

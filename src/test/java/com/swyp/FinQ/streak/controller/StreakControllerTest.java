package com.swyp.FinQ.streak.controller;

import com.swyp.FinQ.global.security.token.IssuedTokenPair;
import com.swyp.FinQ.global.security.token.JwtTokenProvider;
import com.swyp.FinQ.streak.config.StreakConfig;
import com.swyp.FinQ.streak.domain.StreakLog;
import com.swyp.FinQ.streak.repository.StreakLogRepository;
import com.swyp.FinQ.support.MySqlContainerSupport;
import com.swyp.FinQ.user.domain.ProfileImageCode;
import com.swyp.FinQ.user.domain.User;
import com.swyp.FinQ.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class StreakControllerTest extends MySqlContainerSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StreakLogRepository streakLogRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void getsCurrentAndLongestStreakStatus() throws Exception {
        User user = saveUser();
        LocalDate today = LocalDate.now(StreakConfig.STREAK_ZONE_ID);
        saveStreakLogs(
                user,
                today.minusDays(10),
                today.minusDays(9),
                today.minusDays(8),
                today.minusDays(1),
                today
        );

        mockMvc.perform(get("/streak/status")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(user.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("스트릭 상태 조회에 성공했습니다."))
                .andExpect(jsonPath("$.data.currentStreak").value(2))
                .andExpect(jsonPath("$.data.longestStreak").value(3))
                .andExpect(jsonPath("$.data.daysUntilNextBonus").value(3));
    }

    @Test
    void getsCurrentMonthCalendarByDefault() throws Exception {
        User user = saveUser();
        LocalDate today = LocalDate.now(StreakConfig.STREAK_ZONE_ID);
        saveStreakLogs(user, today);

        mockMvc.perform(get("/streak/calendar")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(user.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("월간 스트릭 조회에 성공했습니다."))
                .andExpect(jsonPath("$.data.month").value(YearMonth.from(today).toString()))
                .andExpect(jsonPath("$.data.streakDates[0]").value(today.toString()));
    }

    @Test
    void rejectsMonthBeforeSignUp() throws Exception {
        User user = saveUser();
        YearMonth beforeSignUp = YearMonth.from(user.getCreatedAt()).minusMonths(1);

        mockMvc.perform(get("/streak/calendar")
                        .queryParam("month", beforeSignUp.toString())
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(user.getId())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("STREAK_MONTH_OUT_OF_RANGE"))
                .andExpect(jsonPath("$.message").value("조회할 수 없는 기간입니다."));
    }

    @Test
    void rejectsFutureMonth() throws Exception {
        User user = saveUser();
        YearMonth futureMonth = YearMonth.now(StreakConfig.STREAK_ZONE_ID).plusMonths(1);

        mockMvc.perform(get("/streak/calendar")
                        .queryParam("month", futureMonth.toString())
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(user.getId())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("STREAK_MONTH_OUT_OF_RANGE"));
    }

    @Test
    void rejectsRequestWithoutAccessToken() throws Exception {
        mockMvc.perform(get("/streak/status"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("AUTH_UNAUTHORIZED"));
    }

    private User saveUser() {
        return userRepository.saveAndFlush(User.builder()
                .email("streak-controller@example.com")
                .password("encoded-password")
                .nickname("Minter")
                .profileImageCode(ProfileImageCode.PROFILE_01)
                .build());
    }

    private void saveStreakLogs(User user, LocalDate... streakDates) {
        for (LocalDate streakDate : streakDates) {
            streakLogRepository.save(StreakLog.builder()
                    .user(user)
                    .streakDate(streakDate)
                    .build());
        }
        streakLogRepository.flush();
    }

    private String bearerToken(Long userId) {
        IssuedTokenPair tokens = jwtTokenProvider.issue(userId);
        return "Bearer " + tokens.accessToken();
    }
}

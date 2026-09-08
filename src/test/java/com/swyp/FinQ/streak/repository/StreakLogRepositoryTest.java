package com.swyp.FinQ.streak.repository;

import com.swyp.FinQ.global.config.JpaAuditingConfig;
import com.swyp.FinQ.streak.domain.StreakLog;
import com.swyp.FinQ.support.MySqlContainerSupport;
import com.swyp.FinQ.user.domain.ProfileImageCode;
import com.swyp.FinQ.user.domain.User;
import com.swyp.FinQ.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(JpaAuditingConfig.class)
class StreakLogRepositoryTest extends MySqlContainerSupport {

    @Autowired
    private StreakLogRepository streakLogRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("사용자의 스트릭 기록을 날짜 오름차순으로 조회한다")
    void findsStreakLogsInDateRange() {
        User user = userRepository.save(createUser());
        saveStreakLog(user, LocalDate.of(2026, 9, 3));
        saveStreakLog(user, LocalDate.of(2026, 9, 1));
        saveStreakLog(user, LocalDate.of(2026, 8, 31));

        List<StreakLog> streakLogs = streakLogRepository
                .findAllByUserIdAndStreakDateBetweenOrderByStreakDateAsc(
                        user.getId(),
                        LocalDate.of(2026, 9, 1),
                        LocalDate.of(2026, 9, 30)
                );

        assertThat(streakLogs)
                .extracting(StreakLog::getStreakDate)
                .containsExactly(
                        LocalDate.of(2026, 9, 1),
                        LocalDate.of(2026, 9, 3)
                );
        assertThat(streakLogRepository.existsByUserIdAndStreakDate(
                user.getId(),
                LocalDate.of(2026, 9, 1)
        )).isTrue();
    }

    @Test
    @DisplayName("같은 사용자의 같은 날짜 스트릭은 중복 저장할 수 없다")
    void preventsDuplicateStreakDate() {
        User user = userRepository.save(createUser());
        LocalDate streakDate = LocalDate.of(2026, 9, 8);
        saveStreakLog(user, streakDate);
        entityManager.flush();

        assertThatThrownBy(() -> streakLogRepository.saveAndFlush(StreakLog.builder()
                        .user(user)
                        .streakDate(streakDate)
                        .build()))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("사용자 삭제 시 스트릭 기록도 함께 삭제한다")
    void deletesStreakLogsWithUser() {
        User user = userRepository.save(createUser());
        saveStreakLog(user, LocalDate.of(2026, 9, 8));
        entityManager.flush();
        entityManager.clear();

        userRepository.deleteById(user.getId());
        entityManager.flush();
        entityManager.clear();

        assertThat(streakLogRepository.findAll()).isEmpty();
    }

    private void saveStreakLog(User user, LocalDate streakDate) {
        streakLogRepository.save(StreakLog.builder()
                .user(user)
                .streakDate(streakDate)
                .build());
    }

    private User createUser() {
        return User.builder()
                .email("streak@example.com")
                .password("encoded-password")
                .nickname("Minter")
                .profileImageCode(ProfileImageCode.PROFILE_01)
                .build();
    }
}

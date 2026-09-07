package com.swyp.FinQ.reward.service;

import com.swyp.FinQ.reward.domain.Level;
import com.swyp.FinQ.reward.domain.XpType;
import com.swyp.FinQ.reward.dto.info.XpResultInfo;
import com.swyp.FinQ.reward.repository.XpHistoryRepository;
import com.swyp.FinQ.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class XpGrantServiceTest {

    @InjectMocks
    private XpGrantService xpGrantServiceService;

    @Mock
    private XpHistoryRepository xpHistoryRepository;

    private User createUser(Long id, int totalXp) {
        return User.builder()
                .id(id)
                .email("test@test.com")
                .nickname("테스트")
                .totalXp(totalXp)
                .build();
    }

    @Nested
    @DisplayName("콘텐츠 완료 XP 지급")
    class GrantContentCompletionXp {

        @Test
        @DisplayName("신규 콘텐츠 최초 완료 시 10XP가 지급된다")
        void grantContentCompletionXp_success() {
            User user = createUser(1L, 0);
            Long contentId = 5L;

            given(xpHistoryRepository.existsByUserIdAndXpTypeAndReferenceId(
                    1L, XpType.CONTENT_COMPLETE, "content:5")).willReturn(false);
            given(xpHistoryRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

            XpResultInfo result = xpGrantServiceService.grantContentCompletionXp(user, contentId);

            assertThat(result.xpEarned()).isEqualTo(10);
            assertThat(result.totalXp()).isEqualTo(10);
            assertThat(user.getTotalXp()).isEqualTo(10);
            verify(xpHistoryRepository).save(any());
        }

        @Test
        @DisplayName("이미 완료한 콘텐츠는 0XP가 반환되고 저장되지 않는다")
        void grantContentCompletionXp_duplicate() {
            User user = createUser(1L, 50);
            Long contentId = 5L;

            given(xpHistoryRepository.existsByUserIdAndXpTypeAndReferenceId(
                    1L, XpType.CONTENT_COMPLETE, "content:5")).willReturn(true);

            XpResultInfo result = xpGrantServiceService.grantContentCompletionXp(user, contentId);

            assertThat(result.xpEarned()).isEqualTo(0);
            assertThat(result.totalXp()).isEqualTo(50);
            assertThat(result.leveledUp()).isFalse();
            verify(xpHistoryRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("심화퀴즈 완료 XP 지급")
    class GrantQuizCompletionXp {

        @Test
        @DisplayName("심화퀴즈 최초 통과 시 30XP가 지급된다")
        void grantQuizCompletionXp_success() {
            User user = createUser(1L, 70);
            Long categoryId = 2L;

            given(xpHistoryRepository.existsByUserIdAndXpTypeAndReferenceId(
                    1L, XpType.QUIZ_COMPLETE, "category:2")).willReturn(false);
            given(xpHistoryRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

            XpResultInfo result = xpGrantServiceService.grantQuizCompletionXp(user, categoryId);

            assertThat(result.xpEarned()).isEqualTo(30);
            assertThat(result.totalXp()).isEqualTo(100);
            assertThat(user.getTotalXp()).isEqualTo(100);
            verify(xpHistoryRepository).save(any());
        }

        @Test
        @DisplayName("심화퀴즈 재응시 시 0XP가 반환되고 저장되지 않는다")
        void grantQuizCompletionXp_duplicate() {
            User user = createUser(1L, 100);
            Long categoryId = 2L;

            given(xpHistoryRepository.existsByUserIdAndXpTypeAndReferenceId(
                    1L, XpType.QUIZ_COMPLETE, "category:2")).willReturn(true);

            XpResultInfo result = xpGrantServiceService.grantQuizCompletionXp(user, categoryId);

            assertThat(result.xpEarned()).isEqualTo(0);
            assertThat(result.totalXp()).isEqualTo(100);
            verify(xpHistoryRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("레벨업 판정")
    class LevelUp {

        @Test
        @DisplayName("XP 지급으로 레벨이 올라가면 leveledUp이 true이다")
        void levelUp_detected() {
            User user = createUser(1L, 70);
            Long contentId = 10L;

            given(xpHistoryRepository.existsByUserIdAndXpTypeAndReferenceId(
                    1L, XpType.CONTENT_COMPLETE, "content:10")).willReturn(false);
            given(xpHistoryRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

            XpResultInfo result = xpGrantServiceService.grantContentCompletionXp(user, contentId);

            assertThat(result.previousLevel()).isEqualTo(Level.LV1);
            assertThat(result.currentLevel()).isEqualTo(Level.LV2);
            assertThat(result.leveledUp()).isTrue();
        }

        @Test
        @DisplayName("XP 지급 후에도 같은 레벨이면 leveledUp이 false이다")
        void levelUp_not_detected() {
            User user = createUser(1L, 0);
            Long contentId = 10L;

            given(xpHistoryRepository.existsByUserIdAndXpTypeAndReferenceId(
                    1L, XpType.CONTENT_COMPLETE, "content:10")).willReturn(false);
            given(xpHistoryRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

            XpResultInfo result = xpGrantServiceService.grantContentCompletionXp(user, contentId);

            assertThat(result.previousLevel()).isEqualTo(Level.LV1);
            assertThat(result.currentLevel()).isEqualTo(Level.LV1);
            assertThat(result.leveledUp()).isFalse();
        }
    }
}

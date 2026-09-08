package com.swyp.FinQ.learning.service;

import com.swyp.FinQ.content.domain.Category;
import com.swyp.FinQ.content.domain.Content;
import com.swyp.FinQ.learning.dto.res.ContentAnswerResponse.ContentResult;
import com.swyp.FinQ.learning.dto.res.QuizAnswerResponse.CategoryResult;
import com.swyp.FinQ.learning.repository.UserCategoryCompletionRepository;
import com.swyp.FinQ.learning.repository.UserContentCompletionRepository;
import com.swyp.FinQ.reward.domain.Level;
import com.swyp.FinQ.reward.dto.info.XpResultInfo;
import com.swyp.FinQ.reward.service.XpGrantService;
import com.swyp.FinQ.streak.dto.info.StreakRecordResult;
import com.swyp.FinQ.streak.service.StreakService;
import com.swyp.FinQ.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LearningCompletionServiceTest {

    @InjectMocks
    private LearningCompletionService learningCompletionService;

    @Mock
    private UserContentCompletionRepository userContentCompletionRepository;

    @Mock
    private UserCategoryCompletionRepository userCategoryCompletionRepository;

    @Mock
    private XpGrantService xpGrantService;

    @Mock
    private StreakService streakService;

    @Mock
    private TransactionTemplate transactionTemplate;

    private static final Long USER_ID = 1L;

    private User createUser() {
        return User.builder()
                .id(USER_ID)
                .email("test@test.com")
                .nickname("테스트")
                .totalXp(0)
                .build();
    }

    private Content createContent(Long id) {
        return Content.builder()
                .id(id)
                .contentCode("C" + id)
                .title("테스트 콘텐츠")
                .displayOrder(1)
                .build();
    }

    private Category createCategory(Long id) {
        return Category.builder()
                .id(id)
                .categoryName("테스트 카테고리")
                .displayOrder(1)
                .build();
    }

    @Nested
    @DisplayName("콘텐츠 완료 처리")
    class HandleContentCompletion {

        @Test
        @DisplayName("최초 완료 시 XP가 지급되고 ContentResult가 반환된다")
        void first_completion_grants_xp() {
            User user = createUser();
            Content content = createContent(1L);
            XpResultInfo xpResult = XpResultInfo.granted(10, 50, Level.LV1, Level.LV1);

            given(transactionTemplate.execute(any())).willAnswer(invocation -> {
                var callback = invocation.getArgument(0, org.springframework.transaction.support.TransactionCallback.class);
                return callback.doInTransaction(null);
            });
            given(userContentCompletionRepository.existsByUserIdAndContentId(USER_ID, 1L))
                    .willReturn(false);
            given(userContentCompletionRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
            given(xpGrantService.grantContentCompletionXp(user, 1L)).willReturn(xpResult);
            given(streakService.recordDailyStreak(user)).willReturn(noBonusResult(1, 50, Level.LV1));

            Optional<ContentResult> result = learningCompletionService.handleContentCompletion(user, content);

            assertThat(result).isPresent();
            assertThat(result.get().earnedXp()).isEqualTo(10);
            assertThat(result.get().levelUp()).isFalse();
            assertThat(result.get().newLevel()).isNull();
        }

        @Test
        @DisplayName("최초 완료 시 레벨업하면 newLevel이 반환된다")
        void first_completion_with_level_up() {
            User user = createUser();
            Content content = createContent(1L);
            XpResultInfo xpResult = XpResultInfo.granted(10, 80, Level.LV1, Level.LV2);

            given(transactionTemplate.execute(any())).willAnswer(invocation -> {
                var callback = invocation.getArgument(0, org.springframework.transaction.support.TransactionCallback.class);
                return callback.doInTransaction(null);
            });
            given(userContentCompletionRepository.existsByUserIdAndContentId(USER_ID, 1L))
                    .willReturn(false);
            given(userContentCompletionRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
            given(xpGrantService.grantContentCompletionXp(user, 1L)).willReturn(xpResult);
            given(streakService.recordDailyStreak(user)).willReturn(noBonusResult(1, 80, Level.LV2));

            Optional<ContentResult> result = learningCompletionService.handleContentCompletion(user, content);

            assertThat(result).isPresent();
            assertThat(result.get().levelUp()).isTrue();
            assertThat(result.get().newLevel()).isEqualTo(2);
        }

        @Test
        @DisplayName("콘텐츠 XP와 스트릭 보너스를 합산하고 보너스로 인한 레벨업을 반환한다")
        void firstCompletionWithStreakBonus() {
            User user = createUser();
            Content content = createContent(1L);
            XpResultInfo contentXpResult = XpResultInfo.granted(10, 75, Level.LV1, Level.LV1);
            XpResultInfo streakXpResult = XpResultInfo.granted(5, 80, Level.LV1, Level.LV2);

            given(transactionTemplate.execute(any())).willAnswer(invocation -> {
                var callback = invocation.getArgument(0, org.springframework.transaction.support.TransactionCallback.class);
                return callback.doInTransaction(null);
            });
            given(userContentCompletionRepository.existsByUserIdAndContentId(USER_ID, 1L))
                    .willReturn(false);
            given(userContentCompletionRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
            given(xpGrantService.grantContentCompletionXp(user, 1L)).willReturn(contentXpResult);
            given(streakService.recordDailyStreak(user))
                    .willReturn(new StreakRecordResult(true, 5, streakXpResult));

            Optional<ContentResult> result = learningCompletionService.handleContentCompletion(user, content);

            assertThat(result).isPresent();
            assertThat(result.get().earnedXp()).isEqualTo(15);
            assertThat(result.get().levelUp()).isTrue();
            assertThat(result.get().newLevel()).isEqualTo(2);
        }

        @Test
        @DisplayName("이미 완료한 콘텐츠이면 빈 Optional을 반환한다")
        void already_completed_returns_empty() {
            User user = createUser();
            Content content = createContent(1L);

            given(transactionTemplate.execute(any())).willAnswer(invocation -> {
                var callback = invocation.getArgument(0, org.springframework.transaction.support.TransactionCallback.class);
                return callback.doInTransaction(null);
            });
            given(userContentCompletionRepository.existsByUserIdAndContentId(USER_ID, 1L))
                    .willReturn(true);

            Optional<ContentResult> result = learningCompletionService.handleContentCompletion(user, content);

            assertThat(result).isEmpty();
            verify(xpGrantService, never()).grantContentCompletionXp(any(User.class), any());
            verify(streakService, never()).recordDailyStreak(any(User.class));
        }

        @Test
        @DisplayName("동시 요청으로 DataIntegrityViolationException 발생 시 빈 Optional을 반환한다")
        void concurrent_request_returns_empty() {
            User user = createUser();
            Content content = createContent(1L);

            given(transactionTemplate.execute(any()))
                    .willThrow(new DataIntegrityViolationException("Duplicate entry"));

            Optional<ContentResult> result = learningCompletionService.handleContentCompletion(user, content);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("카테고리 완료 처리")
    class HandleCategoryCompletion {

        @Test
        @DisplayName("최초 완료 시 XP가 지급되고 CategoryResult가 반환된다")
        void first_completion_grants_xp() {
            User user = createUser();
            Category category = createCategory(1L);
            XpResultInfo xpResult = XpResultInfo.granted(30, 100, Level.LV1, Level.LV2);

            given(transactionTemplate.execute(any())).willAnswer(invocation -> {
                var callback = invocation.getArgument(0, org.springframework.transaction.support.TransactionCallback.class);
                return callback.doInTransaction(null);
            });
            given(userCategoryCompletionRepository.existsByUserIdAndCategoryId(USER_ID, 1L))
                    .willReturn(false);
            given(userCategoryCompletionRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
            given(xpGrantService.grantQuizCompletionXp(user, 1L)).willReturn(xpResult);
            given(streakService.recordDailyStreak(user)).willReturn(noBonusResult(1, 100, Level.LV2));

            Optional<CategoryResult> result = learningCompletionService.handleCategoryCompletion(user, category);

            assertThat(result).isPresent();
            assertThat(result.get().earnedXp()).isEqualTo(30);
            assertThat(result.get().levelUp()).isTrue();
            assertThat(result.get().newLevel()).isEqualTo(2);
        }

        @Test
        @DisplayName("심화퀴즈 XP와 스트릭 보너스를 합산한다")
        void firstCompletionWithStreakBonus() {
            User user = createUser();
            Category category = createCategory(1L);
            XpResultInfo quizXpResult = XpResultInfo.granted(30, 130, Level.LV2, Level.LV2);
            XpResultInfo streakXpResult = XpResultInfo.granted(10, 140, Level.LV2, Level.LV2);

            given(transactionTemplate.execute(any())).willAnswer(invocation -> {
                var callback = invocation.getArgument(0, org.springframework.transaction.support.TransactionCallback.class);
                return callback.doInTransaction(null);
            });
            given(userCategoryCompletionRepository.existsByUserIdAndCategoryId(USER_ID, 1L))
                    .willReturn(false);
            given(userCategoryCompletionRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
            given(xpGrantService.grantQuizCompletionXp(user, 1L)).willReturn(quizXpResult);
            given(streakService.recordDailyStreak(user))
                    .willReturn(new StreakRecordResult(true, 15, streakXpResult));

            Optional<CategoryResult> result = learningCompletionService.handleCategoryCompletion(user, category);

            assertThat(result).isPresent();
            assertThat(result.get().earnedXp()).isEqualTo(40);
            assertThat(result.get().levelUp()).isFalse();
            assertThat(result.get().newLevel()).isNull();
        }

        @Test
        @DisplayName("이미 완료한 카테고리이면 빈 Optional을 반환한다")
        void already_completed_returns_empty() {
            User user = createUser();
            Category category = createCategory(1L);

            given(transactionTemplate.execute(any())).willAnswer(invocation -> {
                var callback = invocation.getArgument(0, org.springframework.transaction.support.TransactionCallback.class);
                return callback.doInTransaction(null);
            });
            given(userCategoryCompletionRepository.existsByUserIdAndCategoryId(USER_ID, 1L))
                    .willReturn(true);

            Optional<CategoryResult> result = learningCompletionService.handleCategoryCompletion(user, category);

            assertThat(result).isEmpty();
            verify(xpGrantService, never()).grantQuizCompletionXp(any(User.class), any());
            verify(streakService, never()).recordDailyStreak(any(User.class));
        }

        @Test
        @DisplayName("동시 요청으로 DataIntegrityViolationException 발생 시 빈 Optional을 반환한다")
        void concurrent_request_returns_empty() {
            User user = createUser();
            Category category = createCategory(1L);

            given(transactionTemplate.execute(any()))
                    .willThrow(new DataIntegrityViolationException("Duplicate entry"));

            Optional<CategoryResult> result = learningCompletionService.handleCategoryCompletion(user, category);

            assertThat(result).isEmpty();
        }
    }

    private StreakRecordResult noBonusResult(int currentStreak, int totalXp, Level level) {
        return new StreakRecordResult(true, currentStreak, XpResultInfo.skipped(totalXp, level));
    }
}

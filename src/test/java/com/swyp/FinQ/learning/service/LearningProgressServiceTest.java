package com.swyp.FinQ.learning.service;

import com.swyp.FinQ.content.domain.Content;
import com.swyp.FinQ.learning.repository.UserCategoryCompletionRepository;
import com.swyp.FinQ.learning.repository.UserContentCompletionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class LearningProgressServiceTest {

    @InjectMocks
    private LearningProgressService learningProgressService;

    @Mock
    private UserContentCompletionRepository userContentCompletionRepository;

    @Mock
    private UserCategoryCompletionRepository userCategoryCompletionRepository;

    @Nested
    @DisplayName("진행률 계산")
    class CalculateProgressRate {

        @Test
        @DisplayName("완료 2개 / 전체 5개이면 40%이다")
        void calculateProgressRate_normal() {
            int result = learningProgressService.calculateProgressRate(2, 5);
            assertThat(result).isEqualTo(40);
        }

        @Test
        @DisplayName("전체가 0이면 0%를 반환한다")
        void calculateProgressRate_zeroTotal() {
            int result = learningProgressService.calculateProgressRate(0, 0);
            assertThat(result).isEqualTo(0);
        }

        @Test
        @DisplayName("전부 완료하면 100%이다")
        void calculateProgressRate_allCompleted() {
            int result = learningProgressService.calculateProgressRate(5, 5);
            assertThat(result).isEqualTo(100);
        }

        @Test
        @DisplayName("소수점은 버림한다 (1/3 = 33%)")
        void calculateProgressRate_truncated() {
            int result = learningProgressService.calculateProgressRate(1, 3);
            assertThat(result).isEqualTo(33);
        }
    }

    @Nested
    @DisplayName("카테고리 완료 여부")
    class IsCategoryCompleted {

        @Test
        @DisplayName("완료한 카테고리이면 true를 반환한다")
        void isCategoryCompleted_true() {
            given(userCategoryCompletionRepository.existsByUserIdAndCategoryId(1L, 1L))
                    .willReturn(true);

            assertThat(learningProgressService.isCategoryCompleted(1L, 1L)).isTrue();
        }

        @Test
        @DisplayName("완료하지 않은 카테고리이면 false를 반환한다")
        void isCategoryCompleted_false() {
            given(userCategoryCompletionRepository.existsByUserIdAndCategoryId(1L, 1L))
                    .willReturn(false);

            assertThat(learningProgressService.isCategoryCompleted(1L, 1L)).isFalse();
        }
    }

    @Nested
    @DisplayName("완료 콘텐츠 ID 조회")
    class GetCompletedContentIds {

        @Test
        @DisplayName("완료한 콘텐츠 ID 목록을 반환한다")
        void getCompletedContentIds_returnsIds() {
            List<Content> contents = List.of(
                    Content.builder().build(),
                    Content.builder().build()
            );

            given(userContentCompletionRepository.findCompletedContentIdsByUserIdAndContentIn(1L, contents))
                    .willReturn(Set.of(1L, 2L));

            Set<Long> result = learningProgressService.getCompletedContentIds(1L, contents);
            assertThat(result).containsExactlyInAnyOrder(1L, 2L);
        }

        @Test
        @DisplayName("완료한 콘텐츠가 없으면 빈 Set을 반환한다")
        void getCompletedContentIds_empty() {
            List<Content> contents = List.of(Content.builder().build());

            given(userContentCompletionRepository.findCompletedContentIdsByUserIdAndContentIn(1L, contents))
                    .willReturn(Set.of());

            Set<Long> result = learningProgressService.getCompletedContentIds(1L, contents);
            assertThat(result).isEmpty();
        }
    }
}

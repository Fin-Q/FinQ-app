package com.swyp.FinQ.home.service;

import com.swyp.FinQ.content.domain.Category;
import com.swyp.FinQ.content.domain.CategoryCode;
import com.swyp.FinQ.content.domain.Content;
import com.swyp.FinQ.content.repository.ContentRepository;
import com.swyp.FinQ.home.dto.res.HomeResponse;
import com.swyp.FinQ.learning.repository.UserContentCompletionRepository;
import com.swyp.FinQ.streak.service.StreakQueryService;
import com.swyp.FinQ.user.domain.User;
import com.swyp.FinQ.user.domain.UserInterest;
import com.swyp.FinQ.user.repository.UserInterestRepository;
import com.swyp.FinQ.user.repository.UserRepository;
import org.springframework.data.domain.Pageable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class HomeQueryServiceTest {

    @InjectMocks
    private HomeQueryService homeQueryService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserInterestRepository userInterestRepository;

    @Mock
    private ContentRepository contentRepository;

    @Mock
    private UserContentCompletionRepository userContentCompletionRepository;

    @Mock
    private StreakQueryService streakQueryService;

    @Test
    void usesLogBasedCurrentStreakInsteadOfStoredValue() {
        User user = User.builder()
                .id(1L)
                .nickname("Minter")
                .totalXp(0)
                .currentStreak(7)
                .build();
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(userInterestRepository.findAllWithCategoryByUserId(1L)).willReturn(java.util.List.of());
        given(streakQueryService.getCurrentStreak(1L)).willReturn(0);

        HomeResponse response = homeQueryService.getHome(1L);

        assertThat(response.currentStreak()).isZero();
    }

    @Test
    @DisplayName("같은 날이면 캐시된 질문 카드를 반환하고 캐시를 갱신하지 않는다")
    void returnsCachedCardsWhenSameDay() {
        LocalDate today = LocalDate.now();
        Category category = Category.builder()
                .id(2L)
                .categoryCode(CategoryCode.SAL)
                .categoryName("월급 관리")
                .displayOrder(1)
                .build();
        Content cachedContent = Content.builder()
                .id(10L)
                .contentCode("SAL-01")
                .category(category)
                .title("캐시된 콘텐츠")
                .displayOrder(1)
                .isPremium(false)
                .build();
        User user = User.builder()
                .id(1L)
                .nickname("Minter")
                .totalXp(0)
                .homeQuestionDate(today)
                .homeQuestionIds("10")
                .build();

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(contentRepository.findAllByIdWithCategory(List.of(10L))).willReturn(List.of(cachedContent));
        given(userContentCompletionRepository.findCompletedContentIdsByUserIdAndContentIn(
                eq(1L), any())).willReturn(Set.of());
        given(streakQueryService.getCurrentStreak(1L)).willReturn(0);

        HomeResponse response = homeQueryService.getHome(1L);

        assertThat(response.questions()).hasSize(1);
        assertThat(response.questions().get(0).contentId()).isEqualTo(10L);
        verify(userRepository, never()).updateHomeQuestionCache(any(), any(), any());
    }

    @Test
    @DisplayName("날짜가 다르면 새 질문 카드를 생성하고 캐시를 갱신한다")
    void rebuildsCardsWhenDateChanged() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        Category category = Category.builder()
                .id(2L)
                .categoryCode(CategoryCode.SAL)
                .categoryName("월급 관리")
                .displayOrder(1)
                .build();
        Content newContent = Content.builder()
                .id(20L)
                .contentCode("SAL-02")
                .category(category)
                .title("새 콘텐츠")
                .displayOrder(1)
                .isPremium(false)
                .build();
        UserInterest interest = UserInterest.builder()
                .user(User.builder().id(1L).build())
                .category(category)
                .build();
        User user = User.builder()
                .id(1L)
                .nickname("Minter")
                .totalXp(0)
                .homeQuestionDate(yesterday)
                .homeQuestionIds("10")
                .build();

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(userInterestRepository.findAllWithCategoryByUserId(1L)).willReturn(List.of(interest));
        given(contentRepository.findIncompleteContentsByCategory(
                eq(1L), eq(2L), any(), any(Pageable.class))).willReturn(List.of(newContent));
        given(userContentCompletionRepository.findCompletedContentIdsByUserIdAndContentIn(
                eq(1L), any())).willReturn(Set.of());
        given(streakQueryService.getCurrentStreak(1L)).willReturn(0);

        HomeResponse response = homeQueryService.getHome(1L);

        assertThat(response.questions()).isNotEmpty();
        verify(userRepository).updateHomeQuestionCache(eq(1L), eq(LocalDate.now()), any());
    }

    @Test
    @DisplayName("관심 카테고리가 없으면 빈 질문 카드 리스트를 반환한다")
    void returnsEmptyQuestionsWhenNoInterests() {
        User user = User.builder()
                .id(1L)
                .nickname("Minter")
                .totalXp(0)
                .build();
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(userInterestRepository.findAllWithCategoryByUserId(1L)).willReturn(List.of());
        given(streakQueryService.getCurrentStreak(1L)).willReturn(0);

        HomeResponse response = homeQueryService.getHome(1L);

        assertThat(response.questions()).isEmpty();
    }

    @Test
    void returnsNumericContentIdInQuestionCard() {
        User user = User.builder()
                .id(1L)
                .nickname("Minter")
                .totalXp(0)
                .build();
        Category category = Category.builder()
                .id(2L)
                .categoryCode(CategoryCode.SAL)
                .categoryName("월급 관리")
                .displayOrder(1)
                .build();
        Content content = Content.builder()
                .id(42L)
                .contentCode("SAL-01")
                .category(category)
                .title("월급 관리의 시작")
                .displayOrder(1)
                .isPremium(false)
                .build();
        UserInterest interest = UserInterest.builder()
                .user(user)
                .category(category)
                .build();

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(userInterestRepository.findAllWithCategoryByUserId(1L)).willReturn(List.of(interest));
        given(contentRepository.findIncompleteContentsByCategoryNoExclude(
                org.mockito.ArgumentMatchers.eq(1L),
                org.mockito.ArgumentMatchers.eq(2L),
                any(Pageable.class)
        )).willReturn(List.of(content));
        given(userContentCompletionRepository.findCompletedContentIdsByUserIdAndContentIn(
                1L, List.of(content)
        )).willReturn(Set.of());
        given(streakQueryService.getCurrentStreak(1L)).willReturn(0);

        HomeResponse response = homeQueryService.getHome(1L);

        assertThat(response.questions()).singleElement()
                .extracting(HomeResponse.QuestionCard::contentId)
                .isEqualTo(42L);
    }
}

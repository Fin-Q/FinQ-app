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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

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

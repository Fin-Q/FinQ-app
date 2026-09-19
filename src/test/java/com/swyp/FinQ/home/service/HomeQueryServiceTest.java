package com.swyp.FinQ.home.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swyp.FinQ.content.domain.Category;
import com.swyp.FinQ.content.domain.CategoryCode;
import com.swyp.FinQ.content.domain.Content;
import com.swyp.FinQ.content.repository.ContentRepository;
import com.swyp.FinQ.home.dto.res.HomeResponse;
import com.swyp.FinQ.learning.repository.UserContentCompletionRepository;
import com.swyp.FinQ.reward.domain.Level;
import com.swyp.FinQ.streak.service.StreakQueryService;
import com.swyp.FinQ.user.domain.User;
import com.swyp.FinQ.user.domain.UserInterest;
import com.swyp.FinQ.user.repository.UserInterestRepository;
import com.swyp.FinQ.user.repository.UserRepository;
import org.springframework.data.domain.Pageable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import org.springframework.test.util.ReflectionTestUtils;

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

    @Mock
    private CharacterImageUrlResolver characterImageUrlResolver;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(homeQueryService, "timezone", "Asia/Seoul");
    }

    @ParameterizedTest
    @CsvSource({
            "0, LV1, 1, character_01.png",
            "80, LV2, 2, character_02.png",
            "180, LV3, 3, character_03.png",
            "300, LV4, 4, character_04.png"
    })
    void returnsCharacterImageUrlForCurrentLevel(
            int totalXp,
            Level level,
            int expectedLevel,
            String fileName
    ) {
        User user = User.builder()
                .id(1L)
                .nickname("Minter")
                .totalXp(totalXp)
                .build();
        String imageUrl = "https://assets.example.com/character-images/" + fileName;
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(userInterestRepository.findAllWithCategoryByUserId(1L)).willReturn(List.of());
        given(streakQueryService.getCurrentStreak(1L)).willReturn(0);
        given(characterImageUrlResolver.resolve(level)).willReturn(imageUrl);

        HomeResponse response = homeQueryService.getHome(1L);

        assertThat(response.level()).isEqualTo(expectedLevel);
        assertThat(response.characterStage()).isEqualTo(expectedLevel);
        assertThat(response.characterImageUrl()).isEqualTo(imageUrl);
    }

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
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        Category category = Category.builder()
                .id(2L)
                .categoryCode(CategoryCode.SAL)
                .categoryName("월급 관리")
                .displayOrder(1)
                .build();
        Content cachedContent1 = Content.builder()
                .id(10L)
                .contentCode("SAL-01")
                .category(category)
                .title("캐시된 콘텐츠 1")
                .bodyData("[{\"order\":1,\"title\":\"월급은 왜 남지 않을까요?\",\"content\":[]}]")
                .displayOrder(1)
                .isPremium(false)
                .build();
        Content cachedContent2 = Content.builder()
                .id(11L)
                .contentCode("SAL-02")
                .category(category)
                .title("캐시된 콘텐츠 2")
                .displayOrder(2)
                .isPremium(false)
                .build();
        Content cachedContent3 = Content.builder()
                .id(12L)
                .contentCode("SAL-03")
                .category(category)
                .title("캐시된 콘텐츠 3")
                .displayOrder(3)
                .isPremium(false)
                .build();
        User user = User.builder()
                .id(1L)
                .nickname("Minter")
                .totalXp(0)
                .homeQuestionDate(today)
                .homeQuestionIds("10,11,12")
                .build();

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(contentRepository.findAllByIdWithCategory(List.of(10L, 11L, 12L)))
                .willReturn(List.of(cachedContent1, cachedContent2, cachedContent3));
        given(userContentCompletionRepository.findCompletedContentIdsByUserIdAndContentIn(
                eq(1L), any())).willReturn(Set.of());
        given(streakQueryService.getCurrentStreak(1L)).willReturn(0);

        HomeResponse response = homeQueryService.getHome(1L);

        assertThat(response.questions()).hasSize(3);
        assertThat(response.questions().get(0).contentId()).isEqualTo(10L);
        assertThat(response.questions().get(0).title()).isEqualTo("월급은 왜 남지 않을까요?");
        verify(userRepository, never()).updateHomeQuestionCache(any(), any(), any());
    }

    @Test
    void returnsQuestionTitleFromFirstBodyBlock() {
        Category category = category(2L, CategoryCode.SAL, "월급 관리");
        User user = User.builder().id(1L).nickname("Minter").totalXp(0).build();
        UserInterest interest = UserInterest.builder().user(user).category(category).build();
        Content content = Content.builder()
                .id(10L)
                .contentCode("SAL-01")
                .category(category)
                .title("현금흐름")
                .bodyData("[{\"order\":2,\"title\":\"두 번째 페이지\",\"content\":[]},"
                        + "{\"order\":1,\"title\":\"월급은 들어왔는데 왜 매달 남는 돈이 없을까요?\",\"content\":[]}]")
                .displayOrder(1)
                .isPremium(false)
                .build();

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(userInterestRepository.findAllWithCategoryByUserId(1L)).willReturn(List.of(interest));
        given(contentRepository.findIncompleteContentsByCategoryNoExclude(
                eq(1L), eq(2L), any(Pageable.class)
        )).willReturn(List.of(content));
        given(userContentCompletionRepository.findCompletedContentIdsByUserIdAndContentIn(
                1L, List.of(content)
        )).willReturn(Set.of());
        given(streakQueryService.getCurrentStreak(1L)).willReturn(0);

        HomeResponse response = homeQueryService.getHome(1L);

        assertThat(response.questions()).singleElement()
                .extracting(HomeResponse.QuestionCard::title)
                .isEqualTo("월급은 들어왔는데 왜 매달 남는 돈이 없을까요?");
    }

    @Test
    void fallsBackToContentTitleWhenBodyDataIsInvalid() {
        Category category = category(2L, CategoryCode.SAL, "월급 관리");
        User user = User.builder().id(1L).nickname("Minter").totalXp(0).build();
        UserInterest interest = UserInterest.builder().user(user).category(category).build();
        Content content = Content.builder()
                .id(10L)
                .contentCode("SAL-01")
                .category(category)
                .title("현금흐름")
                .bodyData("invalid-json")
                .displayOrder(1)
                .isPremium(false)
                .build();

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(userInterestRepository.findAllWithCategoryByUserId(1L)).willReturn(List.of(interest));
        given(contentRepository.findIncompleteContentsByCategoryNoExclude(
                eq(1L), eq(2L), any(Pageable.class)
        )).willReturn(List.of(content));
        given(userContentCompletionRepository.findCompletedContentIdsByUserIdAndContentIn(
                1L, List.of(content)
        )).willReturn(Set.of());
        given(streakQueryService.getCurrentStreak(1L)).willReturn(0);

        HomeResponse response = homeQueryService.getHome(1L);

        assertThat(response.questions()).singleElement()
                .extracting(HomeResponse.QuestionCard::title)
                .isEqualTo("현금흐름");
    }

    @Test
    void rebuildsPartialCacheAndFillsThreeQuestions() {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        Category category = category(2L, CategoryCode.SAL, "월급 관리");
        Content cached = content(10L, "SAL-01", category, 1);
        Content fresh = content(20L, "SAL-02", category, 2);
        Content fallback = content(30L, "SAL-03", category, 3);
        User user = User.builder()
                .id(1L)
                .nickname("Minter")
                .totalXp(0)
                .homeQuestionDate(today)
                .homeQuestionIds("10,11,12")
                .build();
        UserInterest interest = UserInterest.builder()
                .user(user)
                .category(category)
                .build();

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(contentRepository.findAllByIdWithCategory(List.of(10L, 11L, 12L)))
                .willReturn(List.of(cached));
        given(userInterestRepository.findAllWithCategoryByUserId(1L)).willReturn(List.of(interest));
        given(contentRepository.findIncompleteContentsByCategory(
                eq(1L), eq(2L), eq(List.of(10L, 11L, 12L)), any(Pageable.class)
        )).willReturn(List.of(fresh));
        given(contentRepository.findIncompleteContentsByCategories(
                eq(1L), eq(List.of(2L)), any(Pageable.class)
        )).willReturn(List.of(cached, fresh, fallback));
        given(userContentCompletionRepository.findCompletedContentIdsByUserIdAndContentIn(
                eq(1L), any())).willReturn(Set.of());
        given(streakQueryService.getCurrentStreak(1L)).willReturn(0);

        HomeResponse response = homeQueryService.getHome(1L);

        assertThat(response.questions())
                .hasSize(3)
                .extracting(HomeResponse.QuestionCard::contentId)
                .doesNotHaveDuplicates();
        verify(userRepository).updateHomeQuestionCache(eq(1L), eq(today), any());
    }

    @Test
    void returnsThreeQuestionsForSingleInterest() {
        Category category = category(2L, CategoryCode.SAL, "월급 관리");
        User user = User.builder().id(1L).nickname("Minter").totalXp(0).build();
        UserInterest interest = UserInterest.builder().user(user).category(category).build();
        List<Content> contents = List.of(
                content(10L, "SAL-01", category, 1),
                content(11L, "SAL-02", category, 2),
                content(12L, "SAL-03", category, 3)
        );

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(userInterestRepository.findAllWithCategoryByUserId(1L)).willReturn(List.of(interest));
        given(contentRepository.findIncompleteContentsByCategoryNoExclude(
                eq(1L), eq(2L), any(Pageable.class)
        )).willReturn(contents);
        given(userContentCompletionRepository.findCompletedContentIdsByUserIdAndContentIn(
                eq(1L), any())).willReturn(Set.of());
        given(streakQueryService.getCurrentStreak(1L)).willReturn(0);

        HomeResponse response = homeQueryService.getHome(1L);

        assertThat(response.questions())
                .hasSize(3)
                .extracting(HomeResponse.QuestionCard::contentId)
                .doesNotHaveDuplicates();
    }

    @Test
    void returnsThreeQuestionsWithTwoToOneInterestDistribution() {
        Category firstCategory = category(2L, CategoryCode.SAL, "월급 관리");
        Category secondCategory = category(3L, CategoryCode.INV, "투자 기초");
        User user = User.builder().id(1L).nickname("Minter").totalXp(0).build();
        List<UserInterest> interests = List.of(
                UserInterest.builder().user(user).category(firstCategory).build(),
                UserInterest.builder().user(user).category(secondCategory).build()
        );
        boolean oddDay = LocalDate.now(ZoneId.of("Asia/Seoul")).getDayOfYear() % 2 == 1;
        Category primary = oddDay ? firstCategory : secondCategory;
        Category secondary = oddDay ? secondCategory : firstCategory;
        List<Content> primaryContents = List.of(
                content(10L, "PRIMARY-01", primary, 1),
                content(11L, "PRIMARY-02", primary, 2)
        );
        Content secondaryContent = content(12L, "SECONDARY-01", secondary, 1);

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(userInterestRepository.findAllWithCategoryByUserId(1L)).willReturn(interests);
        given(contentRepository.findIncompleteContentsByCategoryNoExclude(
                eq(1L), eq(primary.getId()), any(Pageable.class)
        )).willReturn(primaryContents);
        given(contentRepository.findIncompleteContentsByCategory(
                eq(1L), eq(secondary.getId()), any(), any(Pageable.class)
        )).willReturn(List.of(secondaryContent));
        given(userContentCompletionRepository.findCompletedContentIdsByUserIdAndContentIn(
                eq(1L), any())).willReturn(Set.of());
        given(streakQueryService.getCurrentStreak(1L)).willReturn(0);

        HomeResponse response = homeQueryService.getHome(1L);

        assertThat(response.questions()).hasSize(3);
        assertThat(response.questions().stream()
                .filter(question -> question.categoryCode().equals(primary.getCategoryCode().name())))
                .hasSize(2);
        assertThat(response.questions().stream()
                .filter(question -> question.categoryCode().equals(secondary.getCategoryCode().name())))
                .hasSize(1);
    }

    @Test
    @DisplayName("날짜가 다르면 새 질문 카드를 생성하고 캐시를 갱신한다")
    void rebuildsCardsWhenDateChanged() {
        LocalDate yesterday = LocalDate.now(ZoneId.of("Asia/Seoul")).minusDays(1);
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
        verify(userRepository).updateHomeQuestionCache(eq(1L), eq(LocalDate.now(ZoneId.of("Asia/Seoul"))), any());
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

    private Category category(Long id, CategoryCode code, String name) {
        return Category.builder()
                .id(id)
                .categoryCode(code)
                .categoryName(name)
                .displayOrder(id.intValue())
                .build();
    }

    private Content content(Long id, String code, Category category, int displayOrder) {
        return Content.builder()
                .id(id)
                .contentCode(code)
                .category(category)
                .title("추천 콘텐츠 " + id)
                .displayOrder(displayOrder)
                .isPremium(false)
                .build();
    }
}

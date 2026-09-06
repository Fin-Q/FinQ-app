package com.swyp.FinQ.content.service;

import com.swyp.FinQ.content.domain.Category;
import com.swyp.FinQ.content.domain.CategoryCode;
import com.swyp.FinQ.content.domain.Content;
import com.swyp.FinQ.content.dto.res.CategoryDetailResponse;
import com.swyp.FinQ.content.dto.res.KnowledgeMapResponse;
import com.swyp.FinQ.content.repository.CategoryContentCount;
import com.swyp.FinQ.content.repository.CategoryRepository;
import com.swyp.FinQ.content.repository.ContentRepository;
import com.swyp.FinQ.global.exception.BaseException;
import com.swyp.FinQ.learning.service.LearningProgressService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ContentQueryServiceTest {

    @InjectMocks
    private ContentQueryService contentQueryService;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ContentRepository contentRepository;

    @Mock
    private LearningProgressService learningProgressService;

    private Category createCategory(Long id, CategoryCode code, String name) {
        return Category.builder()
                .id(id)
                .categoryCode(code)
                .categoryName(name)
                .displayOrder(1)
                .build();
    }

    private Content createContent(Long id, String code, String title, Category category, boolean isPremium, int order) {
        return Content.builder()
                .id(id)
                .contentCode(code)
                .title(title)
                .description(title + " 설명")
                .category(category)
                .isPremium(isPremium)
                .displayOrder(order)
                .build();
    }

    @Nested
    @DisplayName("지식맵 조회")
    class GetKnowledgeMap {

        @Test
        @DisplayName("전체 카테고리의 진행률을 반환한다")
        void getKnowledgeMap_success() {
            Long userId = 1L;
            Category category = createCategory(1L, CategoryCode.SAL, "월급 관리");

            given(categoryRepository.findAllByOrderByDisplayOrder()).willReturn(List.of(category));
            given(contentRepository.countContentPerCategory()).willReturn(List.of(
                    createCategoryContentCount(1L, 5L)
            ));
            given(contentRepository.countCompletedContentPerCategory(userId)).willReturn(List.of(
                    createCategoryContentCount(1L, 2L)
            ));
            given(learningProgressService.getCompletedCategoryIds(userId)).willReturn(Set.of());
            given(learningProgressService.calculateProgressRate(2, 5)).willReturn(40);

            KnowledgeMapResponse response = contentQueryService.getKnowledgeMap(userId);

            assertThat(response.categories()).hasSize(1);
            KnowledgeMapResponse.CategoryProgress progress = response.categories().get(0);
            assertThat(progress.categoryCode()).isEqualTo(CategoryCode.SAL);
            assertThat(progress.completedContentCount()).isEqualTo(2);
            assertThat(progress.totalContentCount()).isEqualTo(5);
            assertThat(progress.progressRate()).isEqualTo(40);
            assertThat(progress.categoryCompleted()).isFalse();
        }

        @Test
        @DisplayName("완료한 카테고리는 categoryCompleted가 true이다")
        void getKnowledgeMap_categoryCompleted() {
            Long userId = 1L;
            Category category = createCategory(1L, CategoryCode.SAL, "월급 관리");

            given(categoryRepository.findAllByOrderByDisplayOrder()).willReturn(List.of(category));
            given(contentRepository.countContentPerCategory()).willReturn(List.of(
                    createCategoryContentCount(1L, 5L)
            ));
            given(contentRepository.countCompletedContentPerCategory(userId)).willReturn(List.of(
                    createCategoryContentCount(1L, 5L)
            ));
            given(learningProgressService.getCompletedCategoryIds(userId)).willReturn(Set.of(1L));
            given(learningProgressService.calculateProgressRate(5, 5)).willReturn(100);

            KnowledgeMapResponse response = contentQueryService.getKnowledgeMap(userId);

            assertThat(response.categories().get(0).categoryCompleted()).isTrue();
            assertThat(response.categories().get(0).progressRate()).isEqualTo(100);
        }

        private CategoryContentCount createCategoryContentCount(Long categoryId, Long count) {
            return new CategoryContentCount() {
                @Override
                public Long getCategoryId() { return categoryId; }
                @Override
                public Long getContentCount() { return count; }
            };
        }
    }

    @Nested
    @DisplayName("카테고리 상세 조회")
    class GetCategoryDetail {

        @Test
        @DisplayName("카테고리의 콘텐츠 목록과 완료 상태를 반환한다")
        void getCategoryDetail_success() {
            Long userId = 1L;
            Category category = createCategory(1L, CategoryCode.SAL, "월급 관리");
            Content content1 = createContent(1L, "SAL-01", "콘텐츠1", category, false, 1);
            Content content2 = createContent(2L, "SAL-02", "콘텐츠2", category, false, 2);
            List<Content> contents = List.of(content1, content2);

            given(categoryRepository.findByCategoryCode(CategoryCode.SAL)).willReturn(Optional.of(category));
            given(contentRepository.findByCategoryOrderByDisplayOrder(category)).willReturn(contents);
            given(learningProgressService.getCompletedContentIds(eq(userId), any())).willReturn(Set.of(1L));
            given(learningProgressService.isCategoryCompleted(userId, 1L)).willReturn(false);
            given(learningProgressService.calculateProgressRate(1, 2)).willReturn(50);

            CategoryDetailResponse response = contentQueryService.getCategoryDetail(CategoryCode.SAL, userId);

            assertThat(response.categoryCode()).isEqualTo(CategoryCode.SAL);
            assertThat(response.completedContentCount()).isEqualTo(1);
            assertThat(response.totalContentCount()).isEqualTo(2);
            assertThat(response.progressRate()).isEqualTo(50);
            assertThat(response.advancedQuizStatus()).isEqualTo("INCOMPLETE");
            assertThat(response.contents()).hasSize(2);
            assertThat(response.contents().get(0).completionStatus()).isEqualTo("COMPLETED");
            assertThat(response.contents().get(1).completionStatus()).isEqualTo("INCOMPLETE");
        }

        @Test
        @DisplayName("프리미엄 콘텐츠는 premiumContents에 분리된다")
        void getCategoryDetail_premiumSeparated() {
            Long userId = 1L;
            Category category = createCategory(1L, CategoryCode.SAL, "월급 관리");
            Content normal = createContent(1L, "SAL-01", "일반", category, false, 1);
            Content premium = createContent(2L, "SAL-P1", "프리미엄", category, true, 2);

            given(categoryRepository.findByCategoryCode(CategoryCode.SAL)).willReturn(Optional.of(category));
            given(contentRepository.findByCategoryOrderByDisplayOrder(category)).willReturn(List.of(normal, premium));
            given(learningProgressService.getCompletedContentIds(eq(userId), any())).willReturn(Set.of());
            given(learningProgressService.isCategoryCompleted(userId, 1L)).willReturn(false);
            given(learningProgressService.calculateProgressRate(0, 1)).willReturn(0);

            CategoryDetailResponse response = contentQueryService.getCategoryDetail(CategoryCode.SAL, userId);

            assertThat(response.contents()).hasSize(1);
            assertThat(response.contents().get(0).title()).isEqualTo("일반");
            assertThat(response.premiumContents()).hasSize(1);
            assertThat(response.premiumContents().get(0).title()).isEqualTo("프리미엄");
        }

        @Test
        @DisplayName("존재하지 않는 카테고리 코드이면 예외가 발생한다")
        void getCategoryDetail_notFound() {
            given(categoryRepository.findByCategoryCode(CategoryCode.SAL)).willReturn(Optional.empty());

            assertThatThrownBy(() -> contentQueryService.getCategoryDetail(CategoryCode.SAL, 1L))
                    .isInstanceOf(BaseException.class);
        }
    }
}
package com.swyp.FinQ.content.service;

import com.swyp.FinQ.content.domain.Category;
import com.swyp.FinQ.content.domain.CategoryCode;
import com.swyp.FinQ.content.domain.Content;
import com.swyp.FinQ.content.domain.ContentQuestion;
import com.swyp.FinQ.content.domain.ContentStage;
import com.swyp.FinQ.content.domain.QuestionType;
import com.swyp.FinQ.content.dto.res.CategoryDetailResponse;
import com.swyp.FinQ.content.dto.res.ContentDetailResponse;
import com.swyp.FinQ.content.dto.res.ContentDetailResponse.BlockResponse;
import com.swyp.FinQ.content.dto.res.KnowledgeMapResponse;
import com.swyp.FinQ.content.repository.CategoryContentCount;
import com.swyp.FinQ.content.repository.CategoryRepository;
import com.swyp.FinQ.content.repository.ContentQuestionRepository;
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
import java.util.Map;
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
    private ContentQuestionRepository contentQuestionRepository;

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

    @Nested
    @DisplayName("콘텐츠 상세 조회")
    class GetContentDetail {

        private ContentQuestion createQuestion(Long id, Content content, ContentStage stage,
                                                QuestionType type, String body,
                                                String optionA, String optionB,
                                                String optionC, String optionD, String answer) {
            return ContentQuestion.builder()
                    .id(id)
                    .questionCode("Q-" + id)
                    .content(content)
                    .contentStage(stage)
                    .questionType(type)
                    .questionBody(body)
                    .explanation("해설")
                    .optionA(optionA)
                    .optionB(optionB)
                    .optionC(optionC)
                    .optionD(optionD)
                    .correctAnswer(answer)
                    .build();
        }

        @Test
        @DisplayName("BODY, SUMMARY, QUESTION 블록이 order 순으로 정렬된다")
        void getContentDetail_blocksOrderedCorrectly() {
            Category category = createCategory(1L, CategoryCode.SAL, "월급 관리");
            Content content = Content.builder()
                    .id(1L)
                    .contentCode("SAL-01")
                    .title("월급 관리의 시작")
                    .category(category)
                    .source("금융감독원")
                    .displayOrder(1)
                    .isPremium(false)
                    .bodyData("[{\"bodyType\":\"EXPLANATION\",\"order\":1,\"title\":\"제목\",\"description\":\"설명\"}]")
                    .summaryContent("핵심 정리 내용")
                    .build();

            ContentQuestion question = createQuestion(1L, content, ContentStage.P1, QuestionType.OX,
                    "문제입니다", "맞다", "틀리다", null, null, "O");

            given(contentRepository.findByIdWithCategory(1L)).willReturn(Optional.of(content));
            given(contentRepository.countByCategoryAndIsPremiumFalse(category)).willReturn(4);
            given(contentQuestionRepository.findByContent(content)).willReturn(List.of(question));

            ContentDetailResponse response = contentQueryService.getContentDetail(1L);

            assertThat(response.blocks()).hasSize(3);
            assertThat(response.blocks().get(0).blockType()).isEqualTo("BODY");
            assertThat(response.blocks().get(0).order()).isEqualTo(1);
            assertThat(response.blocks().get(1).blockType()).isEqualTo("QUESTION");
            assertThat(response.blocks().get(1).order()).isEqualTo(2);
            assertThat(response.blocks().get(2).blockType()).isEqualTo("SUMMARY");
            assertThat(response.blocks().get(2).order()).isEqualTo(5);
        }

        @Test
        @DisplayName("EXPLANATION 타입은 description과 additionalDescription을 포함한다")
        void getContentDetail_explanationBody() {
            Category category = createCategory(1L, CategoryCode.SAL, "월급 관리");
            Content content = Content.builder()
                    .id(1L)
                    .contentCode("SAL-01")
                    .title("제목")
                    .category(category)
                    .displayOrder(1)
                    .isPremium(false)
                    .bodyData("[{\"bodyType\":\"EXPLANATION\",\"order\":1,\"title\":\"본문 제목\",\"description\":\"본문 설명\",\"additionalDescription\":\"추가 설명\"}]")
                    .build();

            given(contentRepository.findByIdWithCategory(1L)).willReturn(Optional.of(content));
            given(contentRepository.countByCategoryAndIsPremiumFalse(category)).willReturn(1);
            given(contentQuestionRepository.findByContent(content)).willReturn(List.of());

            ContentDetailResponse response = contentQueryService.getContentDetail(1L);

            assertThat(response.blocks()).hasSize(1);
            BlockResponse block = response.blocks().get(0);
            assertThat(block.bodyType()).isEqualTo("EXPLANATION");
            Map<String, Object> body = (Map<String, Object>) block.body();
            assertThat(body.get("title")).isEqualTo("본문 제목");
            assertThat(body.get("description")).isEqualTo("본문 설명");
            assertThat(body.get("additionalDescription")).isEqualTo("추가 설명");
        }

        @Test
        @DisplayName("CASE 타입은 imageUrl과 description을 포함한다")
        void getContentDetail_caseBody() {
            Category category = createCategory(1L, CategoryCode.SAL, "월급 관리");
            Content content = Content.builder()
                    .id(1L)
                    .contentCode("SAL-01")
                    .title("제목")
                    .category(category)
                    .displayOrder(1)
                    .isPremium(false)
                    .bodyData("[{\"bodyType\":\"CASE\",\"order\":1,\"title\":\"사례\",\"description\":\"사례 설명\",\"imageUrl\":\"https://img.com/case.png\"}]")
                    .build();

            given(contentRepository.findByIdWithCategory(1L)).willReturn(Optional.of(content));
            given(contentRepository.countByCategoryAndIsPremiumFalse(category)).willReturn(1);
            given(contentQuestionRepository.findByContent(content)).willReturn(List.of());

            ContentDetailResponse response = contentQueryService.getContentDetail(1L);

            Map<String, Object> body = (Map<String, Object>) response.blocks().get(0).body();
            assertThat(body.get("title")).isEqualTo("사례");
            assertThat(body.get("imageUrl")).isEqualTo("https://img.com/case.png");
            assertThat(body.get("description")).isEqualTo("사례 설명");
            assertThat(body).doesNotContainKey("additionalDescription");
        }

        @Test
        @DisplayName("COMPARISON 타입은 tableImageUrl과 imageUrl을 포함한다")
        void getContentDetail_comparisonBody() {
            Category category = createCategory(1L, CategoryCode.SAL, "월급 관리");
            Content content = Content.builder()
                    .id(1L)
                    .contentCode("SAL-01")
                    .title("제목")
                    .category(category)
                    .displayOrder(1)
                    .isPremium(false)
                    .bodyData("[{\"bodyType\":\"COMPARISON\",\"order\":1,\"title\":\"비교\",\"description\":\"비교 설명\",\"tableImageUrl\":\"https://img.com/table.png\",\"imageUrl\":\"https://img.com/img.png\"}]")
                    .build();

            given(contentRepository.findByIdWithCategory(1L)).willReturn(Optional.of(content));
            given(contentRepository.countByCategoryAndIsPremiumFalse(category)).willReturn(1);
            given(contentQuestionRepository.findByContent(content)).willReturn(List.of());

            ContentDetailResponse response = contentQueryService.getContentDetail(1L);

            Map<String, Object> body = (Map<String, Object>) response.blocks().get(0).body();
            assertThat(body.get("tableImageUrl")).isEqualTo("https://img.com/table.png");
            assertThat(body.get("imageUrl")).isEqualTo("https://img.com/img.png");
            assertThat(body.get("description")).isEqualTo("비교 설명");
        }

        @Test
        @DisplayName("OX 문제는 O, X 두 개의 선택지를 반환한다")
        void getContentDetail_oxOptions() {
            Category category = createCategory(1L, CategoryCode.SAL, "월급 관리");
            Content content = Content.builder()
                    .id(1L)
                    .contentCode("SAL-01")
                    .title("제목")
                    .category(category)
                    .displayOrder(1)
                    .isPremium(false)
                    .build();

            ContentQuestion oxQuestion = createQuestion(1L, content, ContentStage.P1, QuestionType.OX,
                    "OX 문제입니다", "맞다", "틀리다", null, null, "O");

            given(contentRepository.findByIdWithCategory(1L)).willReturn(Optional.of(content));
            given(contentRepository.countByCategoryAndIsPremiumFalse(category)).willReturn(1);
            given(contentQuestionRepository.findByContent(content)).willReturn(List.of(oxQuestion));

            ContentDetailResponse response = contentQueryService.getContentDetail(1L);

            BlockResponse questionBlock = response.blocks().get(0);
            assertThat(questionBlock.questionType()).isEqualTo("OX");
            assertThat(questionBlock.options()).hasSize(2);
            assertThat(questionBlock.options().get(0).optionId()).isEqualTo("O");
            assertThat(questionBlock.options().get(1).optionId()).isEqualTo("X");
        }

        @Test
        @DisplayName("SINGLE_CHOICE 문제는 A, B, C, D 네 개의 선택지를 반환한다")
        void getContentDetail_singleChoiceOptions() {
            Category category = createCategory(1L, CategoryCode.SAL, "월급 관리");
            Content content = Content.builder()
                    .id(1L)
                    .contentCode("SAL-01")
                    .title("제목")
                    .category(category)
                    .displayOrder(1)
                    .isPremium(false)
                    .build();

            ContentQuestion scQuestion = createQuestion(1L, content, ContentStage.P2, QuestionType.SINGLE_CHOICE,
                    "객관식 문제입니다", "보기A", "보기B", "보기C", "보기D", "A");

            given(contentRepository.findByIdWithCategory(1L)).willReturn(Optional.of(content));
            given(contentRepository.countByCategoryAndIsPremiumFalse(category)).willReturn(1);
            given(contentQuestionRepository.findByContent(content)).willReturn(List.of(scQuestion));

            ContentDetailResponse response = contentQueryService.getContentDetail(1L);

            BlockResponse questionBlock = response.blocks().get(0);
            assertThat(questionBlock.questionType()).isEqualTo("SINGLE_CHOICE");
            assertThat(questionBlock.options()).hasSize(4);
            assertThat(questionBlock.options().get(0).optionId()).isEqualTo("A");
            assertThat(questionBlock.options().get(3).optionId()).isEqualTo("D");
        }

        @Test
        @DisplayName("존재하지 않는 콘텐츠 ID이면 예외가 발생한다")
        void getContentDetail_notFound() {
            given(contentRepository.findByIdWithCategory(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> contentQueryService.getContentDetail(999L))
                    .isInstanceOf(BaseException.class);
        }
    }
}
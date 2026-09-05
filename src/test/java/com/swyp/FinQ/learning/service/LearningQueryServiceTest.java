package com.swyp.FinQ.learning.service;

import com.swyp.FinQ.content.domain.Category;
import com.swyp.FinQ.content.repository.CategoryRepository;
import com.swyp.FinQ.global.exception.BaseException;
import com.swyp.FinQ.learning.domain.AdvancedQuiz;
import com.swyp.FinQ.learning.dto.res.QuizListResponse;
import com.swyp.FinQ.learning.repository.AdvancedQuizRepository;
import com.swyp.FinQ.reward.domain.XpConstants;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class LearningQueryServiceTest {

    @InjectMocks
    private LearningQueryService learningQueryService;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private AdvancedQuizRepository advancedQuizRepository;

    private Category createCategory(Long id, String name) {
        return Category.builder()
                .id(id)
                .categoryName(name)
                .displayOrder(1)
                .build();
    }

    private AdvancedQuiz createQuiz(Long id, Category category, int order) {
        return AdvancedQuiz.builder()
                .id(id)
                .questionCode("AQ" + id)
                .category(category)
                .quizOrder(order)
                .questionBody("퀴즈 " + order + "번 문제")
                .explanation("해설")
                .optionA("선택지A")
                .optionB("선택지B")
                .optionC("선택지C")
                .optionD("선택지D")
                .correctAnswer("A")
                .build();
    }

    @Nested
    @DisplayName("심화퀴즈 조회")
    class GetQuizList {

        @Test
        @DisplayName("카테고리의 퀴즈 목록을 순서대로 반환한다")
        void returns_quiz_list_in_order() {
            Category category = createCategory(1L, "금융 기초");
            List<AdvancedQuiz> quizzes = List.of(
                    createQuiz(1L, category, 1),
                    createQuiz(2L, category, 2),
                    createQuiz(3L, category, 3)
            );

            given(categoryRepository.findById(1L)).willReturn(Optional.of(category));
            given(advancedQuizRepository.findByCategoryIdOrderByQuizOrder(1L)).willReturn(quizzes);

            QuizListResponse response = learningQueryService.getQuizList(1L);

            assertThat(response.categoryId()).isEqualTo(1L);
            assertThat(response.categoryName()).isEqualTo("금융 기초");
            assertThat(response.rewardXp()).isEqualTo(XpConstants.QUIZ_COMPLETE_XP);
            assertThat(response.questions()).hasSize(3);
        }

        @Test
        @DisplayName("각 퀴즈 문제에 4개의 선택지가 포함된다")
        void each_question_has_four_options() {
            Category category = createCategory(1L, "금융 기초");
            List<AdvancedQuiz> quizzes = List.of(createQuiz(1L, category, 1));

            given(categoryRepository.findById(1L)).willReturn(Optional.of(category));
            given(advancedQuizRepository.findByCategoryIdOrderByQuizOrder(1L)).willReturn(quizzes);

            QuizListResponse response = learningQueryService.getQuizList(1L);

            QuizListResponse.QuizQuestion question = response.questions().get(0);
            assertThat(question.questionId()).isEqualTo(1L);
            assertThat(question.order()).isEqualTo(1);
            assertThat(question.questionType()).isEqualTo("SINGLE_CHOICE");
            assertThat(question.questionBody()).isEqualTo("퀴즈 1번 문제");
            assertThat(question.options()).hasSize(4);
            assertThat(question.options().get(0).optionId()).isEqualTo("A");
            assertThat(question.options().get(0).optionText()).isEqualTo("선택지A");
        }

        @Test
        @DisplayName("퀴즈가 없는 카테고리이면 빈 목록을 반환한다")
        void empty_quiz_list() {
            Category category = createCategory(1L, "금융 기초");

            given(categoryRepository.findById(1L)).willReturn(Optional.of(category));
            given(advancedQuizRepository.findByCategoryIdOrderByQuizOrder(1L)).willReturn(List.of());

            QuizListResponse response = learningQueryService.getQuizList(1L);

            assertThat(response.questions()).isEmpty();
        }

        @Test
        @DisplayName("존재하지 않는 카테고리이면 예외가 발생한다")
        void category_not_found() {
            given(categoryRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> learningQueryService.getQuizList(99L))
                    .isInstanceOf(BaseException.class);
        }
    }
}

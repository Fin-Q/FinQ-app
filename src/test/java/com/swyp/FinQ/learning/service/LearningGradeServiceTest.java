package com.swyp.FinQ.learning.service;

import com.swyp.FinQ.content.domain.Category;
import com.swyp.FinQ.content.domain.Content;
import com.swyp.FinQ.content.domain.ContentQuestion;
import com.swyp.FinQ.content.domain.ContentStage;
import com.swyp.FinQ.content.domain.QuestionType;
import com.swyp.FinQ.content.repository.CategoryRepository;
import com.swyp.FinQ.content.repository.ContentQuestionRepository;
import com.swyp.FinQ.content.repository.ContentRepository;
import com.swyp.FinQ.global.exception.BaseException;
import com.swyp.FinQ.learning.domain.AdvancedQuiz;
import com.swyp.FinQ.learning.dto.res.ContentAnswerResponse;
import com.swyp.FinQ.learning.dto.res.ContentAnswerResponse.ContentResult;
import com.swyp.FinQ.learning.dto.res.QuizAnswerResponse;
import com.swyp.FinQ.learning.dto.res.QuizAnswerResponse.CategoryResult;
import com.swyp.FinQ.learning.repository.AdvancedQuizRepository;
import com.swyp.FinQ.user.domain.User;
import com.swyp.FinQ.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class LearningGradeServiceTest {

    @InjectMocks
    private LearningGradeService learningGradeService;

    @Mock
    private ContentRepository contentRepository;

    @Mock
    private ContentQuestionRepository contentQuestionRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private AdvancedQuizRepository advancedQuizRepository;

    @Mock
    private LearningCompletionService learningCompletionService;

    @Mock
    private UserRepository userRepository;

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
                .summaryContent("요약 내용")
                .displayOrder(1)
                .build();
    }

    private ContentQuestion createQuestion(Long id, Content content, ContentStage stage,
                                            QuestionType type, String correctAnswer) {
        return ContentQuestion.builder()
                .id(id)
                .questionCode("Q" + id)
                .content(content)
                .contentStage(stage)
                .questionType(type)
                .questionBody("문제 본문")
                .explanation("해설")
                .optionA("선택지A")
                .optionB("선택지B")
                .optionC("선택지C")
                .optionD("선택지D")
                .correctAnswer(correctAnswer)
                .build();
    }

    private Category createCategory(Long id) {
        return Category.builder()
                .id(id)
                .categoryName("테스트 카테고리")
                .displayOrder(1)
                .build();
    }

    private AdvancedQuiz createQuiz(Long id, Category category, int quizOrder, String correctAnswer) {
        return AdvancedQuiz.builder()
                .id(id)
                .questionCode("AQ" + id)
                .category(category)
                .quizOrder(quizOrder)
                .questionBody("퀴즈 본문")
                .explanation("퀴즈 해설")
                .optionA("A선택지")
                .optionB("B선택지")
                .optionC("C선택지")
                .optionD("D선택지")
                .correctAnswer(correctAnswer)
                .build();
    }

    @Nested
    @DisplayName("콘텐츠 문제 채점")
    class GradeContentAnswer {

        @Test
        @DisplayName("정답을 제출하면 correct가 true이고 nextAction이 반환된다")
        void correct_answer() {
            Content content = createContent(1L);
            ContentQuestion question = createQuestion(1L, content, ContentStage.P1,
                    QuestionType.SINGLE_CHOICE, "A");

            given(contentRepository.findById(1L)).willReturn(Optional.of(content));
            given(contentQuestionRepository.findById(1L)).willReturn(Optional.of(question));

            ContentAnswerResponse response = learningGradeService.gradeContentAnswer(
                    USER_ID, 1L, 1L, "A");

            assertThat(response.correct()).isTrue();
            assertThat(response.selectedOptionId()).isEqualTo("A");
            assertThat(response.correctOptionId()).isEqualTo("A");
            assertThat(response.explanation()).isEqualTo("해설");
        }

        @Test
        @DisplayName("오답을 제출하면 correct가 false이고 nextAction이 RETRY이다")
        void incorrect_answer() {
            Content content = createContent(1L);
            ContentQuestion question = createQuestion(1L, content, ContentStage.P1,
                    QuestionType.SINGLE_CHOICE, "A");

            given(contentRepository.findById(1L)).willReturn(Optional.of(content));
            given(contentQuestionRepository.findById(1L)).willReturn(Optional.of(question));

            ContentAnswerResponse response = learningGradeService.gradeContentAnswer(
                    USER_ID, 1L, 1L, "B");

            assertThat(response.correct()).isFalse();
            assertThat(response.nextAction()).isEqualTo("RETRY");
            assertThat(response.selectedOptionId()).isEqualTo("B");
            assertThat(response.correctOptionId()).isEqualTo("A");
        }

        @Test
        @DisplayName("OX 문제에 정답을 제출하면 채점에 성공한다")
        void correct_ox_answer() {
            Content content = createContent(1L);
            ContentQuestion question = createQuestion(1L, content, ContentStage.P1,
                    QuestionType.OX, "O");

            given(contentRepository.findById(1L)).willReturn(Optional.of(content));
            given(contentQuestionRepository.findById(1L)).willReturn(Optional.of(question));

            ContentAnswerResponse response = learningGradeService.gradeContentAnswer(
                    USER_ID, 1L, 1L, "O");

            assertThat(response.correct()).isTrue();
        }

        @Test
        @DisplayName("마지막 문제(F) 정답 시 콘텐츠 완료 처리가 실행된다")
        void correct_final_stage_triggers_completion() {
            User user = createUser();
            Content content = createContent(1L);
            ContentQuestion question = createQuestion(1L, content, ContentStage.F,
                    QuestionType.SINGLE_CHOICE, "A");
            ContentResult contentResult = new ContentResult(10, false, null);

            given(contentRepository.findById(1L)).willReturn(Optional.of(content));
            given(contentQuestionRepository.findById(1L)).willReturn(Optional.of(question));
            given(userRepository.findById(USER_ID)).willReturn(Optional.of(user));
            given(learningCompletionService.handleContentCompletion(user, content))
                    .willReturn(Optional.of(contentResult));

            ContentAnswerResponse response = learningGradeService.gradeContentAnswer(
                    USER_ID, 1L, 1L, "A");

            assertThat(response.correct()).isTrue();
            assertThat(response.nextAction()).isEqualTo("CONTENT_COMPLETED");
            assertThat(response.contentResult()).isNotNull();
            assertThat(response.contentResult().earnedXp()).isEqualTo(10);
        }

        @Test
        @DisplayName("이미 완료한 콘텐츠의 마지막 문제를 다시 풀면 contentResult가 null이다")
        void correct_final_stage_already_completed() {
            User user = createUser();
            Content content = createContent(1L);
            ContentQuestion question = createQuestion(1L, content, ContentStage.F,
                    QuestionType.SINGLE_CHOICE, "A");

            given(contentRepository.findById(1L)).willReturn(Optional.of(content));
            given(contentQuestionRepository.findById(1L)).willReturn(Optional.of(question));
            given(userRepository.findById(USER_ID)).willReturn(Optional.of(user));
            given(learningCompletionService.handleContentCompletion(user, content))
                    .willReturn(Optional.empty());

            ContentAnswerResponse response = learningGradeService.gradeContentAnswer(
                    USER_ID, 1L, 1L, "A");

            assertThat(response.correct()).isTrue();
            assertThat(response.contentResult()).isNull();
        }

        @Test
        @DisplayName("존재하지 않는 콘텐츠이면 예외가 발생한다")
        void content_not_found() {
            given(contentRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> learningGradeService.gradeContentAnswer(
                    USER_ID, 99L, 1L, "A"))
                    .isInstanceOf(BaseException.class);
        }

        @Test
        @DisplayName("존재하지 않는 문제이면 예외가 발생한다")
        void question_not_found() {
            Content content = createContent(1L);
            given(contentRepository.findById(1L)).willReturn(Optional.of(content));
            given(contentQuestionRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> learningGradeService.gradeContentAnswer(
                    USER_ID, 1L, 99L, "A"))
                    .isInstanceOf(BaseException.class);
        }

        @Test
        @DisplayName("문제가 해당 콘텐츠에 속하지 않으면 예외가 발생한다")
        void question_content_mismatch() {
            Content content1 = createContent(1L);
            Content content2 = createContent(2L);
            ContentQuestion question = createQuestion(1L, content2, ContentStage.P1,
                    QuestionType.SINGLE_CHOICE, "A");

            given(contentRepository.findById(1L)).willReturn(Optional.of(content1));
            given(contentQuestionRepository.findById(1L)).willReturn(Optional.of(question));

            assertThatThrownBy(() -> learningGradeService.gradeContentAnswer(
                    USER_ID, 1L, 1L, "A"))
                    .isInstanceOf(BaseException.class);
        }

        @Test
        @DisplayName("유효하지 않은 옵션 ID이면 예외가 발생한다")
        void invalid_option_id() {
            Content content = createContent(1L);
            ContentQuestion question = createQuestion(1L, content, ContentStage.P1,
                    QuestionType.SINGLE_CHOICE, "A");

            given(contentRepository.findById(1L)).willReturn(Optional.of(content));
            given(contentQuestionRepository.findById(1L)).willReturn(Optional.of(question));

            assertThatThrownBy(() -> learningGradeService.gradeContentAnswer(
                    USER_ID, 1L, 1L, "Z"))
                    .isInstanceOf(BaseException.class);
        }

        @Test
        @DisplayName("OX 문제에 A를 제출하면 유효하지 않은 옵션으로 예외가 발생한다")
        void invalid_option_for_ox() {
            Content content = createContent(1L);
            ContentQuestion question = createQuestion(1L, content, ContentStage.P1,
                    QuestionType.OX, "O");

            given(contentRepository.findById(1L)).willReturn(Optional.of(content));
            given(contentQuestionRepository.findById(1L)).willReturn(Optional.of(question));

            assertThatThrownBy(() -> learningGradeService.gradeContentAnswer(
                    USER_ID, 1L, 1L, "A"))
                    .isInstanceOf(BaseException.class);
        }
    }

    @Nested
    @DisplayName("다음 행동 결정 (nextAction)")
    class DetermineNextAction {

        @Test
        @DisplayName("P1 정답 시 요약이 있으면 NEXT_BODY를 반환한다")
        void p1_correct_with_summary_returns_next_body() {
            Content content = createContent(1L);
            ContentQuestion question = createQuestion(1L, content, ContentStage.P1,
                    QuestionType.SINGLE_CHOICE, "A");

            given(contentRepository.findById(1L)).willReturn(Optional.of(content));
            given(contentQuestionRepository.findById(1L)).willReturn(Optional.of(question));

            ContentAnswerResponse response = learningGradeService.gradeContentAnswer(
                    USER_ID, 1L, 1L, "A");

            assertThat(response.nextAction()).isEqualTo("NEXT_BODY");
        }

        @Test
        @DisplayName("P2 정답 시 요약 콘텐츠가 있으면 NEXT_SUMMARY를 반환한다")
        void p2_correct_with_summary_returns_next_summary() {
            Content content = createContent(1L);
            ContentQuestion question = createQuestion(1L, content, ContentStage.P2,
                    QuestionType.SINGLE_CHOICE, "A");

            given(contentRepository.findById(1L)).willReturn(Optional.of(content));
            given(contentQuestionRepository.findById(1L)).willReturn(Optional.of(question));

            ContentAnswerResponse response = learningGradeService.gradeContentAnswer(
                    USER_ID, 1L, 1L, "A");

            // P2 blockOrder=4, next(F) blockOrder=6, SUMMARY_BLOCK_ORDER=5
            // 4 < 5 && 6 > 5 && summaryContent != null → NEXT_SUMMARY
            assertThat(response.nextAction()).isEqualTo("NEXT_SUMMARY");
        }

        @Test
        @DisplayName("P2 정답 시 요약 콘텐츠가 없으면 NEXT_BODY를 반환한다")
        void p2_correct_without_summary_returns_next_body() {
            Content content = Content.builder()
                    .id(1L)
                    .contentCode("C1")
                    .title("테스트")
                    .summaryContent(null)
                    .displayOrder(1)
                    .build();
            ContentQuestion question = createQuestion(1L, content, ContentStage.P2,
                    QuestionType.SINGLE_CHOICE, "A");

            given(contentRepository.findById(1L)).willReturn(Optional.of(content));
            given(contentQuestionRepository.findById(1L)).willReturn(Optional.of(question));

            ContentAnswerResponse response = learningGradeService.gradeContentAnswer(
                    USER_ID, 1L, 1L, "A");

            assertThat(response.nextAction()).isEqualTo("NEXT_BODY");
        }

        @Test
        @DisplayName("F(마지막) 정답 시 CONTENT_COMPLETED를 반환한다")
        void f_correct_returns_content_completed() {
            User user = createUser();
            Content content = createContent(1L);
            ContentQuestion question = createQuestion(1L, content, ContentStage.F,
                    QuestionType.SINGLE_CHOICE, "A");

            given(contentRepository.findById(1L)).willReturn(Optional.of(content));
            given(contentQuestionRepository.findById(1L)).willReturn(Optional.of(question));
            given(userRepository.findById(USER_ID)).willReturn(Optional.of(user));
            given(learningCompletionService.handleContentCompletion(user, content))
                    .willReturn(Optional.empty());

            ContentAnswerResponse response = learningGradeService.gradeContentAnswer(
                    USER_ID, 1L, 1L, "A");

            assertThat(response.nextAction()).isEqualTo("CONTENT_COMPLETED");
        }
    }

    @Nested
    @DisplayName("심화퀴즈 채점")
    class GradeQuizAnswer {

        @Test
        @DisplayName("정답을 제출하면 correct가 true이다")
        void correct_answer() {
            Category category = createCategory(1L);
            AdvancedQuiz quiz = createQuiz(1L, category, 1, "A");

            given(categoryRepository.findById(1L)).willReturn(Optional.of(category));
            given(advancedQuizRepository.findById(1L)).willReturn(Optional.of(quiz));

            QuizAnswerResponse response = learningGradeService.gradeQuizAnswer(
                    USER_ID, 1L, 1L, "A");

            assertThat(response.correct()).isTrue();
            assertThat(response.selectedOptionId()).isEqualTo("A");
            assertThat(response.correctOptionId()).isEqualTo("A");
            assertThat(response.isLastQuestion()).isFalse();
        }

        @Test
        @DisplayName("오답을 제출하면 correct가 false이다")
        void incorrect_answer() {
            Category category = createCategory(1L);
            AdvancedQuiz quiz = createQuiz(1L, category, 1, "A");

            given(categoryRepository.findById(1L)).willReturn(Optional.of(category));
            given(advancedQuizRepository.findById(1L)).willReturn(Optional.of(quiz));

            QuizAnswerResponse response = learningGradeService.gradeQuizAnswer(
                    USER_ID, 1L, 1L, "B");

            assertThat(response.correct()).isFalse();
            assertThat(response.selectedOptionId()).isEqualTo("B");
            assertThat(response.correctOptionId()).isEqualTo("A");
        }

        @Test
        @DisplayName("마지막 문제(3번째)를 정답으로 제출하면 isLastQuestion이 true이다")
        void last_question_correct() {
            User user = createUser();
            Category category = createCategory(1L);
            AdvancedQuiz quiz = createQuiz(1L, category, 3, "B");
            CategoryResult categoryResult = new CategoryResult(30, false, null);

            given(categoryRepository.findById(1L)).willReturn(Optional.of(category));
            given(advancedQuizRepository.findById(1L)).willReturn(Optional.of(quiz));
            given(userRepository.findById(USER_ID)).willReturn(Optional.of(user));
            given(learningCompletionService.handleCategoryCompletion(user, category))
                    .willReturn(Optional.of(categoryResult));

            QuizAnswerResponse response = learningGradeService.gradeQuizAnswer(
                    USER_ID, 1L, 1L, "B");

            assertThat(response.correct()).isTrue();
            assertThat(response.isLastQuestion()).isTrue();
            assertThat(response.categoryResult()).isNotNull();
            assertThat(response.categoryResult().earnedXp()).isEqualTo(30);
        }

        @Test
        @DisplayName("마지막 문제를 오답으로 제출하면 categoryResult가 null이다")
        void last_question_incorrect() {
            Category category = createCategory(1L);
            AdvancedQuiz quiz = createQuiz(1L, category, 3, "B");

            given(categoryRepository.findById(1L)).willReturn(Optional.of(category));
            given(advancedQuizRepository.findById(1L)).willReturn(Optional.of(quiz));

            QuizAnswerResponse response = learningGradeService.gradeQuizAnswer(
                    USER_ID, 1L, 1L, "A");

            assertThat(response.correct()).isFalse();
            assertThat(response.isLastQuestion()).isTrue();
            assertThat(response.categoryResult()).isNull();
        }

        @Test
        @DisplayName("존재하지 않는 카테고리이면 예외가 발생한다")
        void category_not_found() {
            given(categoryRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> learningGradeService.gradeQuizAnswer(
                    USER_ID, 99L, 1L, "A"))
                    .isInstanceOf(BaseException.class);
        }

        @Test
        @DisplayName("존재하지 않는 퀴즈이면 예외가 발생한다")
        void quiz_not_found() {
            Category category = createCategory(1L);
            given(categoryRepository.findById(1L)).willReturn(Optional.of(category));
            given(advancedQuizRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> learningGradeService.gradeQuizAnswer(
                    USER_ID, 1L, 99L, "A"))
                    .isInstanceOf(BaseException.class);
        }

        @Test
        @DisplayName("퀴즈가 해당 카테고리에 속하지 않으면 예외가 발생한다")
        void quiz_category_mismatch() {
            Category category1 = createCategory(1L);
            Category category2 = createCategory(2L);
            AdvancedQuiz quiz = createQuiz(1L, category2, 1, "A");

            given(categoryRepository.findById(1L)).willReturn(Optional.of(category1));
            given(advancedQuizRepository.findById(1L)).willReturn(Optional.of(quiz));

            assertThatThrownBy(() -> learningGradeService.gradeQuizAnswer(
                    USER_ID, 1L, 1L, "A"))
                    .isInstanceOf(BaseException.class);
        }

        @Test
        @DisplayName("유효하지 않은 옵션 ID이면 예외가 발생한다")
        void invalid_option_id() {
            Category category = createCategory(1L);
            AdvancedQuiz quiz = createQuiz(1L, category, 1, "A");

            given(categoryRepository.findById(1L)).willReturn(Optional.of(category));
            given(advancedQuizRepository.findById(1L)).willReturn(Optional.of(quiz));

            assertThatThrownBy(() -> learningGradeService.gradeQuizAnswer(
                    USER_ID, 1L, 1L, "Z"))
                    .isInstanceOf(BaseException.class);
        }
    }
}

package com.swyp.FinQ.learning.service;

import com.swyp.FinQ.content.domain.Content;
import com.swyp.FinQ.content.domain.ContentQuestion;
import com.swyp.FinQ.content.domain.ContentStage;
import com.swyp.FinQ.content.domain.QuestionType;
import com.swyp.FinQ.content.exception.ContentErrorCode;
import com.swyp.FinQ.content.repository.CategoryRepository;
import com.swyp.FinQ.content.repository.ContentQuestionRepository;
import com.swyp.FinQ.content.repository.ContentRepository;
import com.swyp.FinQ.global.exception.BaseException;
import com.swyp.FinQ.learning.domain.AdvancedQuiz;
import com.swyp.FinQ.learning.domain.LearningConstants;
import com.swyp.FinQ.learning.domain.NextAction;
import com.swyp.FinQ.learning.dto.res.ContentAnswerResponse;
import com.swyp.FinQ.learning.dto.res.ContentAnswerResponse.ContentResult;
import com.swyp.FinQ.learning.dto.res.QuizAnswerResponse;
import com.swyp.FinQ.learning.dto.res.QuizAnswerResponse.CategoryResult;
import com.swyp.FinQ.learning.exception.LearningErrorCode;
import com.swyp.FinQ.learning.repository.AdvancedQuizRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LearningGradeService {

    private final ContentRepository contentRepository;
    private final ContentQuestionRepository contentQuestionRepository;
    private final CategoryRepository categoryRepository;
    private final AdvancedQuizRepository advancedQuizRepository;
    private final LearningCompletionService learningCompletionService;

    /**
     * 콘텐츠 문제 채점
     */
    public ContentAnswerResponse gradeContentAnswer(Long userId, Long contentId, Long questionId,
                                                     String selectedOptionId) {
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> BaseException.of(ContentErrorCode.CONTENT_NOT_FOUND));

        ContentQuestion question = contentQuestionRepository.findById(questionId)
                .orElseThrow(() -> BaseException.of(LearningErrorCode.QUESTION_NOT_FOUND));

        if (!question.getContent().getId().equals(contentId)) {
            throw BaseException.of(LearningErrorCode.QUESTION_CONTENT_MISMATCH);
        }

        validateOptionId(selectedOptionId, question.getQuestionType());

        String correctAnswer = question.getCorrectAnswer();
        boolean isCorrect = correctAnswer.equals(selectedOptionId);

        if (!isCorrect) {
            return ContentAnswerResponse.incorrect(
                    question.getExplanation(), selectedOptionId, correctAnswer);
        }

        NextAction nextAction = determineNextAction(question);
        ContentResult contentResult = null;

        if (question.getContentStage() == ContentStage.F) {
            contentResult = learningCompletionService.handleContentCompletion(userId, content).orElse(null);
        }

        return ContentAnswerResponse.correct(
                question.getExplanation(), selectedOptionId, correctAnswer,
                nextAction.name(), contentResult);
    }

    /**
     * 심화퀴즈 채점
     */
    public QuizAnswerResponse gradeQuizAnswer(Long userId, Long categoryId, Long questionId,
                                               String selectedOptionId) {
        categoryRepository.findById(categoryId)
                .orElseThrow(() -> BaseException.of(ContentErrorCode.CATEGORY_NOT_FOUND));

        AdvancedQuiz quiz = advancedQuizRepository.findById(questionId)
                .orElseThrow(() -> BaseException.of(LearningErrorCode.QUIZ_NOT_FOUND));

        if (!quiz.getCategory().getId().equals(categoryId)) {
            throw BaseException.of(LearningErrorCode.QUIZ_CATEGORY_MISMATCH);
        }

        validateOptionId(selectedOptionId, QuestionType.SINGLE_CHOICE);

        String correctAnswer = quiz.getCorrectAnswer();
        boolean isCorrect = correctAnswer.equals(selectedOptionId);
        boolean isLastQuestion = quiz.getQuizOrder() == LearningConstants.TOTAL_QUIZ_COUNT;

        if (!isCorrect) {
            return QuizAnswerResponse.incorrect(
                    quiz.getExplanation(), selectedOptionId, correctAnswer, isLastQuestion);
        }

        CategoryResult categoryResult = null;

        if (isLastQuestion) {
            categoryResult = learningCompletionService.handleCategoryCompletion(userId, quiz.getCategory()).orElse(null);
        }

        return QuizAnswerResponse.correct(
                quiz.getExplanation(), selectedOptionId, correctAnswer,
                isLastQuestion, categoryResult);
    }

    private void validateOptionId(String selectedOptionId, QuestionType questionType) {
        if (!questionType.isValidOption(selectedOptionId)) {
            throw BaseException.of(LearningErrorCode.INVALID_OPTION);
        }
    }

    /**
     * 정답 시 다음 블록 이동 방향 결정
     */
    private NextAction determineNextAction(ContentQuestion question) {
        ContentStage stage = question.getContentStage();

        if (stage == ContentStage.F) {
            return NextAction.CONTENT_COMPLETED;
        }

        Content content = question.getContent();
        int currentBlockOrder = stage.getBlockOrder();
        int nextQuestionBlockOrder = stage.nextQuestionStage().getBlockOrder();

        boolean hasSummaryBetween = (currentBlockOrder < ContentStage.SUMMARY_BLOCK_ORDER
                && nextQuestionBlockOrder > ContentStage.SUMMARY_BLOCK_ORDER)
                && content.getSummaryContent() != null;

        if (hasSummaryBetween) {
            return NextAction.NEXT_SUMMARY;
        }

        if (currentBlockOrder + 1 < nextQuestionBlockOrder) {
            return NextAction.NEXT_BODY;
        }

        return NextAction.NEXT_QUESTION;
    }
}
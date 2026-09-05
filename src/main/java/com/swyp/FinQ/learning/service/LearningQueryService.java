package com.swyp.FinQ.learning.service;

import com.swyp.FinQ.content.domain.Category;
import com.swyp.FinQ.content.exception.ContentErrorCode;
import com.swyp.FinQ.content.repository.CategoryRepository;
import com.swyp.FinQ.global.exception.BaseException;
import com.swyp.FinQ.content.domain.QuestionType;
import com.swyp.FinQ.learning.domain.AdvancedQuiz;
import com.swyp.FinQ.reward.domain.XpConstants;
import com.swyp.FinQ.learning.dto.res.QuizListResponse;
import com.swyp.FinQ.learning.repository.AdvancedQuizRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LearningQueryService {

    private final CategoryRepository categoryRepository;
    private final AdvancedQuizRepository advancedQuizRepository;

    /**
     * 심화퀴즈 조회
     */
    public QuizListResponse getQuizList(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> BaseException.of(ContentErrorCode.CATEGORY_NOT_FOUND));

        List<AdvancedQuiz> quizzes = advancedQuizRepository.findByCategoryIdOrderByQuizOrder(categoryId);

        List<QuizListResponse.QuizQuestion> questions = quizzes.stream()
                .map(quiz -> new QuizListResponse.QuizQuestion(
                        quiz.getId(),
                        quiz.getQuizOrder(),
                        QuestionType.SINGLE_CHOICE.name(),
                        quiz.getQuestionBody(),
                        quiz.getOptions().stream()
                                .map(opt -> new QuizListResponse.Option(opt.getKey(), opt.getValue()))
                                .toList()
                ))
                .toList();

        return new QuizListResponse(
                category.getId(),
                category.getCategoryName(),
                XpConstants.QUIZ_COMPLETE_XP,
                questions
        );
    }
}

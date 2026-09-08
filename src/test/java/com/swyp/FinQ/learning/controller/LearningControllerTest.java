package com.swyp.FinQ.learning.controller;

import com.swyp.FinQ.learning.dto.req.AnswerRequest;
import com.swyp.FinQ.learning.dto.res.ContentAnswerResponse;
import com.swyp.FinQ.learning.dto.res.QuizAnswerResponse;
import com.swyp.FinQ.learning.service.LearningGradeService;
import com.swyp.FinQ.learning.service.LearningQueryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LearningControllerTest {

    @InjectMocks
    private LearningController learningController;

    @Mock
    private LearningGradeService learningGradeService;

    @Mock
    private LearningQueryService learningQueryService;

    @Mock
    private Jwt jwt;

    @Test
    void gradesContentAnswerForAuthenticatedUser() {
        AnswerRequest request = new AnswerRequest("A");
        ContentAnswerResponse response = ContentAnswerResponse.correct(
                "해설", "A", "A", "CONTENT_COMPLETED", null
        );
        given(jwt.getSubject()).willReturn("42");
        given(learningGradeService.gradeContentAnswer(42L, 1L, 2L, "A"))
                .willReturn(response);

        learningController.gradeContentAnswer(jwt, 1L, 2L, request);

        verify(learningGradeService).gradeContentAnswer(42L, 1L, 2L, "A");
    }

    @Test
    void gradesQuizAnswerForAuthenticatedUser() {
        AnswerRequest request = new AnswerRequest("B");
        QuizAnswerResponse response = QuizAnswerResponse.correct(
                "해설", "B", "B", true, null
        );
        given(jwt.getSubject()).willReturn("84");
        given(learningGradeService.gradeQuizAnswer(84L, 3L, 4L, "B"))
                .willReturn(response);

        learningController.gradeQuizAnswer(jwt, 3L, 4L, request);

        verify(learningGradeService).gradeQuizAnswer(84L, 3L, 4L, "B");
    }
}

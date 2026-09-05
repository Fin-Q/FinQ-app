package com.swyp.FinQ.learning.controller;

import com.swyp.FinQ.global.success.SuccessResponse;
import com.swyp.FinQ.learning.dto.req.AnswerRequest;
import com.swyp.FinQ.learning.dto.res.ContentAnswerResponse;
import com.swyp.FinQ.learning.dto.res.QuizAnswerResponse;
import com.swyp.FinQ.learning.dto.res.QuizListResponse;
import com.swyp.FinQ.learning.service.LearningGradeService;
import com.swyp.FinQ.learning.service.LearningQueryService;
import com.swyp.FinQ.learning.success.LearningSuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Learning", description = "학습 채점 및 심화퀴즈 API")
@RestController
@RequiredArgsConstructor
public class LearningController {

    private final LearningGradeService learningGradeService;
    private final LearningQueryService learningQueryService;

    // TODO: 인증 구현 후 토큰에서 userId 추출로 변경
    private static final Long TEMP_USER_ID = 1L;

    @Operation(summary = "콘텐츠 문제 채점", description = "콘텐츠 학습 중 문제의 답안을 제출하고 채점합니다.")
    @PostMapping("/contents/{contentId}/questions/{questionId}/answers")
    public ResponseEntity<SuccessResponse<ContentAnswerResponse>> gradeContentAnswer(
            @Parameter(description = "콘텐츠 ID") @PathVariable Long contentId,
            @Parameter(description = "문제 ID") @PathVariable Long questionId,
            @Valid @RequestBody AnswerRequest request
    ) {
        ContentAnswerResponse response = learningGradeService.gradeContentAnswer(
                TEMP_USER_ID, contentId, questionId, request.selectedOptionId());
        return SuccessResponse.of(LearningSuccessCode.CONTENT_ANSWER_GRADED, response);
    }

    @Operation(summary = "심화퀴즈 조회", description = "카테고리별 심화퀴즈 3문제를 조회합니다.")
    @GetMapping("/categories/{categoryId}/quiz")
    public ResponseEntity<SuccessResponse<QuizListResponse>> getQuizList(
            @Parameter(description = "카테고리 ID") @PathVariable Long categoryId
    ) {
        QuizListResponse response = learningQueryService.getQuizList(categoryId);
        return SuccessResponse.of(LearningSuccessCode.QUIZ_LIST_RETRIEVED, response);
    }

    @Operation(summary = "심화퀴즈 채점", description = "심화퀴즈 답안을 제출하고 채점합니다.")
    @PostMapping("/categories/{categoryId}/quiz/questions/{questionId}/answers")
    public ResponseEntity<SuccessResponse<QuizAnswerResponse>> gradeQuizAnswer(
            @Parameter(description = "카테고리 ID") @PathVariable Long categoryId,
            @Parameter(description = "문제 ID") @PathVariable Long questionId,
            @Valid @RequestBody AnswerRequest request
    ) {
        QuizAnswerResponse response = learningGradeService.gradeQuizAnswer(
                TEMP_USER_ID, categoryId, questionId, request.selectedOptionId());
        return SuccessResponse.of(LearningSuccessCode.QUIZ_ANSWER_GRADED, response);
    }
}

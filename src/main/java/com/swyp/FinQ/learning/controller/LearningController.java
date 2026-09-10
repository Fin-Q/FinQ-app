package com.swyp.FinQ.learning.controller;

import com.swyp.FinQ.global.success.SuccessResponse;
import com.swyp.FinQ.global.config.ApiDocumentation;
import com.swyp.FinQ.global.config.ApiOwner;
import com.swyp.FinQ.global.config.ApiTags;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = ApiTags.LEARNING, description = "학습 채점 및 심화퀴즈 API")
@RestController
@RequiredArgsConstructor
public class LearningController {

    private final LearningGradeService learningGradeService;
    private final LearningQueryService learningQueryService;

    @Operation(
            description = "콘텐츠 학습 중 문제의 답안을 제출하고 채점합니다. 최초 정답 시 콘텐츠 완료 및 보상을 처리합니다."
    )
    @ApiDocumentation(id = "LEARNING-001", name = "문제 채점", owner = ApiOwner.YEZANEE)
    @PostMapping("/contents/{contentId}/questions/{questionId}/answers")
    public ResponseEntity<SuccessResponse<ContentAnswerResponse>> gradeContentAnswer(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "콘텐츠 ID") @PathVariable Long contentId,
            @Parameter(description = "문제 ID") @PathVariable Long questionId,
            @Valid @RequestBody AnswerRequest request
    ) {
        ContentAnswerResponse response = learningGradeService.gradeContentAnswer(
                Long.valueOf(jwt.getSubject()), contentId, questionId, request.selectedOptionId());
        return SuccessResponse.of(LearningSuccessCode.CONTENT_ANSWER_GRADED, response);
    }

    @Operation(
            description = "카테고리별 심화퀴즈 3문제를 조회합니다."
    )
    @ApiDocumentation(id = "LEARNING-002", name = "심화퀴즈 조회", owner = ApiOwner.YEZANEE)
    @GetMapping("/categories/{categoryId}/quiz")
    public ResponseEntity<SuccessResponse<QuizListResponse>> getQuizList(
            @Parameter(description = "카테고리 ID") @PathVariable Long categoryId
    ) {
        QuizListResponse response = learningQueryService.getQuizList(categoryId);
        return SuccessResponse.of(LearningSuccessCode.QUIZ_LIST_RETRIEVED, response);
    }

    @Operation(
            description = "심화퀴즈 답안을 제출하고 채점합니다. 3문제 최초 통과 시 카테고리 완료 및 보상을 처리합니다."
    )
    @ApiDocumentation(id = "LEARNING-003", name = "심화퀴즈 채점", owner = ApiOwner.YEZANEE)
    @PostMapping("/categories/{categoryId}/quiz/questions/{questionId}/answers")
    public ResponseEntity<SuccessResponse<QuizAnswerResponse>> gradeQuizAnswer(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "카테고리 ID") @PathVariable Long categoryId,
            @Parameter(description = "문제 ID") @PathVariable Long questionId,
            @Valid @RequestBody AnswerRequest request
    ) {
        QuizAnswerResponse response = learningGradeService.gradeQuizAnswer(
                Long.valueOf(jwt.getSubject()), categoryId, questionId, request.selectedOptionId());
        return SuccessResponse.of(LearningSuccessCode.QUIZ_ANSWER_GRADED, response);
    }
}

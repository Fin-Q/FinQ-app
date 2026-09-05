package com.swyp.FinQ.learning.service;

import com.swyp.FinQ.content.domain.Category;
import com.swyp.FinQ.content.domain.Content;
import com.swyp.FinQ.learning.domain.UserCategoryCompletion;
import com.swyp.FinQ.learning.domain.UserContentCompletion;
import com.swyp.FinQ.learning.dto.res.ContentAnswerResponse.ContentResult;
import com.swyp.FinQ.learning.dto.res.QuizAnswerResponse.CategoryResult;
import com.swyp.FinQ.learning.repository.UserCategoryCompletionRepository;
import com.swyp.FinQ.learning.repository.UserContentCompletionRepository;
import com.swyp.FinQ.reward.domain.XpConstants;
import com.swyp.FinQ.reward.dto.info.XpResultInfo;
import com.swyp.FinQ.reward.service.XpGrantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LearningCompletionService {

    private final UserContentCompletionRepository userContentCompletionRepository;
    private final UserCategoryCompletionRepository userCategoryCompletionRepository;
    private final XpGrantService xpGrantService;
    private final TransactionTemplate transactionTemplate;

    /**
     * 콘텐츠 최초 완료 처리: 완료 기록 + XP 지급
     */
    public Optional<ContentResult> handleContentCompletion(Long userId, Content content) {
        try {
            return Optional.ofNullable(transactionTemplate.execute(status -> {
                if (userContentCompletionRepository.existsByUserIdAndContentId(userId, content.getId())) {
                    return null;
                }

                UserContentCompletion completion = UserContentCompletion.builder()
                        .userId(userId)
                        .content(content)
                        .completedAt(LocalDateTime.now())
                        .xpEarned(XpConstants.CONTENT_COMPLETE_XP)
                        .build();
                userContentCompletionRepository.save(completion);

                XpResultInfo xpResult = xpGrantService.grantContentCompletionXp(userId, content.getId());

                // TODO: StreakService 구현 후 스트릭 인정 처리 및 스트릭 보너스 XP 합산
                int totalEarnedXp = xpResult.xpEarned();

                return new ContentResult(
                        totalEarnedXp,
                        xpResult.leveledUp(),
                        xpResult.leveledUp() ? xpResult.currentLevel().getValue() : null
                );
            }));
        } catch (DataIntegrityViolationException e) {
            log.warn("콘텐츠 완료 중복 요청 무시: userId={}, contentId={}", userId, content.getId());
            return Optional.empty();
        }
    }

    /**
     * 카테고리 최초 완료 처리 (심화퀴즈 통과): 완료 기록 + XP 지급
     */
    public Optional<CategoryResult> handleCategoryCompletion(Long userId, Category category) {
        try {
            return Optional.ofNullable(transactionTemplate.execute(status -> {
                if (userCategoryCompletionRepository.existsByUserIdAndCategoryId(userId, category.getId())) {
                    return null;
                }

                UserCategoryCompletion completion = UserCategoryCompletion.builder()
                        .userId(userId)
                        .category(category)
                        .completedAt(LocalDateTime.now())
                        .xpEarned(XpConstants.QUIZ_COMPLETE_XP)
                        .build();
                userCategoryCompletionRepository.save(completion);

                XpResultInfo xpResult = xpGrantService.grantQuizCompletionXp(userId, category.getId());

                // TODO: StreakService 구현 후 스트릭 인정 처리 및 스트릭 보너스 XP 합산
                int totalEarnedXp = xpResult.xpEarned();

                return new CategoryResult(
                        totalEarnedXp,
                        xpResult.leveledUp(),
                        xpResult.leveledUp() ? xpResult.currentLevel().getValue() : null
                );
            }));
        } catch (DataIntegrityViolationException e) {
            log.warn("카테고리 완료 중복 요청 무시: userId={}, categoryId={}", userId, category.getId());
            return Optional.empty();
        }
    }
}
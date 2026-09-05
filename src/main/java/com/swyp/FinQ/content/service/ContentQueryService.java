package com.swyp.FinQ.content.service;

import com.swyp.FinQ.content.domain.Category;
import com.swyp.FinQ.content.domain.CategoryCode;
import com.swyp.FinQ.content.domain.Content;
import com.swyp.FinQ.content.dto.res.CategoryDetailResponse;
import com.swyp.FinQ.content.dto.res.KnowledgeMapResponse;
import com.swyp.FinQ.content.exception.ContentErrorCode;
import com.swyp.FinQ.content.repository.CategoryContentCount;
import com.swyp.FinQ.content.repository.CategoryRepository;
import com.swyp.FinQ.content.repository.ContentRepository;
import com.swyp.FinQ.global.exception.BaseException;
import com.swyp.FinQ.learning.service.LearningProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContentQueryService {

    private final CategoryRepository categoryRepository;
    private final ContentRepository contentRepository;
    private final LearningProgressService learningProgressService;

    public KnowledgeMapResponse getKnowledgeMap(Long userId) {
        List<Category> categories = categoryRepository.findAllByOrderByDisplayOrder();
        Map<Long, Long> totalCountMap = toMap(contentRepository.countContentPerCategory());
        Map<Long, Long> completedCountMap = toMap(contentRepository.countCompletedContentPerCategory(userId));
        Set<Long> completedCategoryIds = learningProgressService.getCompletedCategoryIds(userId);

        List<KnowledgeMapResponse.CategoryProgress> progresses = categories.stream()
                .map(category -> {
                    int totalCount = totalCountMap.getOrDefault(category.getId(), 0L).intValue();
                    int completedCount = completedCountMap.getOrDefault(category.getId(), 0L).intValue();

                    return new KnowledgeMapResponse.CategoryProgress(
                            category.getId(),
                            category.getCategoryCode(),
                            category.getCategoryName(),
                            completedCount,
                            totalCount,
                            learningProgressService.calculateProgressRate(completedCount, totalCount),
                            completedCategoryIds.contains(category.getId())
                    );
                })
                .toList();

        return new KnowledgeMapResponse(progresses);
    }

    public CategoryDetailResponse getCategoryDetail(CategoryCode categoryCode, Long userId) {
        Category category = categoryRepository.findByCategoryCode(categoryCode)
                .orElseThrow(() -> BaseException.of(ContentErrorCode.CATEGORY_NOT_FOUND));

        List<Content> allContents = contentRepository.findByCategoryOrderByDisplayOrder(category);
        Set<Long> completedContentIds = learningProgressService.getCompletedContentIds(userId, allContents);

        List<CategoryDetailResponse.ContentSummary> contentSummaries = buildContentSummaries(allContents, completedContentIds);
        List<CategoryDetailResponse.PremiumContentSummary> premiumSummaries = buildPremiumSummaries(allContents);

        int completedCount = (int) allContents.stream()
                .filter(c -> !c.isPremium())
                .filter(c -> completedContentIds.contains(c.getId()))
                .count();
        int totalCount = contentSummaries.size();
        boolean categoryCompleted = learningProgressService.isCategoryCompleted(userId, category.getId());

        return new CategoryDetailResponse(
                category.getId(),
                category.getCategoryCode(),
                category.getCategoryName(),
                completedCount,
                totalCount,
                learningProgressService.calculateProgressRate(completedCount, totalCount),
                categoryCompleted,
                categoryCompleted ? "COMPLETED" : "INCOMPLETE",
                contentSummaries,
                premiumSummaries
        );
    }

    private Map<Long, Long> toMap(List<CategoryContentCount> counts) {
        return counts.stream()
                .collect(Collectors.toMap(
                        CategoryContentCount::getCategoryId,
                        CategoryContentCount::getContentCount
                ));
    }

    private List<CategoryDetailResponse.ContentSummary> buildContentSummaries(
            List<Content> allContents, Set<Long> completedContentIds) {
        return allContents.stream()
                .filter(c -> !c.isPremium())
                .map(content -> new CategoryDetailResponse.ContentSummary(
                        content.getId(),
                        content.getContentCode(),
                        content.getTitle(),
                        content.getDescription(),
                        completedContentIds.contains(content.getId()) ? "COMPLETED" : "INCOMPLETE",
                        content.getDisplayOrder()
                ))
                .toList();
    }

    private List<CategoryDetailResponse.PremiumContentSummary> buildPremiumSummaries(List<Content> allContents) {
        return allContents.stream()
                .filter(Content::isPremium)
                .map(content -> new CategoryDetailResponse.PremiumContentSummary(
                        content.getId(),
                        content.getTitle()
                ))
                .toList();
    }
}

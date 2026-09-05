package com.swyp.FinQ.content.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swyp.FinQ.content.domain.BodyType;
import com.swyp.FinQ.content.domain.Category;
import com.swyp.FinQ.content.domain.CategoryCode;
import com.swyp.FinQ.content.domain.CompletionStatus;
import com.swyp.FinQ.content.domain.Content;
import com.swyp.FinQ.content.domain.ContentQuestion;
import com.swyp.FinQ.content.domain.ContentStage;
import com.swyp.FinQ.content.dto.info.BodyBlockDataInfo;

import com.swyp.FinQ.content.dto.res.CategoryDetailResponse;
import com.swyp.FinQ.content.dto.res.ContentDetailResponse;
import com.swyp.FinQ.content.dto.res.ContentDetailResponse.BlockResponse;
import com.swyp.FinQ.content.dto.res.ContentDetailResponse.OptionResponse;
import com.swyp.FinQ.content.dto.res.KnowledgeMapResponse;
import com.swyp.FinQ.content.exception.ContentErrorCode;
import com.swyp.FinQ.content.repository.CategoryContentCount;
import com.swyp.FinQ.content.repository.CategoryRepository;
import com.swyp.FinQ.content.repository.ContentQuestionRepository;
import com.swyp.FinQ.content.repository.ContentRepository;
import com.swyp.FinQ.global.exception.BaseException;
import com.swyp.FinQ.learning.service.LearningProgressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContentQueryService {

    private final CategoryRepository categoryRepository;
    private final ContentRepository contentRepository;
    private final ContentQuestionRepository contentQuestionRepository;
    private final LearningProgressService learningProgressService;
    private final ObjectMapper objectMapper;

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
        List<Content> freeContents = allContents.stream().filter(c -> !c.isPremium()).toList();
        List<Content> premiumContents = allContents.stream().filter(Content::isPremium).toList();
        Set<Long> completedContentIds = learningProgressService.getCompletedContentIds(userId, allContents);

        List<CategoryDetailResponse.ContentSummary> contentSummaries = buildContentSummaries(freeContents, completedContentIds);
        List<CategoryDetailResponse.PremiumContentSummary> premiumSummaries = buildPremiumSummaries(premiumContents);

        int completedCount = (int) freeContents.stream()
                .filter(c -> completedContentIds.contains(c.getId()))
                .count();
        int totalCount = freeContents.size();
        boolean categoryCompleted = learningProgressService.isCategoryCompleted(userId, category.getId());

        return new CategoryDetailResponse(
                category.getId(),
                category.getCategoryCode(),
                category.getCategoryName(),
                completedCount,
                totalCount,
                learningProgressService.calculateProgressRate(completedCount, totalCount),
                categoryCompleted,
                CompletionStatus.of(categoryCompleted).name(),
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
            List<Content> freeContents, Set<Long> completedContentIds) {
        return freeContents.stream()
                .map(content -> new CategoryDetailResponse.ContentSummary(
                        content.getId(),
                        content.getContentCode(),
                        content.getTitle(),
                        content.getDescription(),
                        CompletionStatus.of(completedContentIds.contains(content.getId())).name(),
                        content.getDisplayOrder()
                ))
                .toList();
    }

    public ContentDetailResponse getContentDetail(Long contentId) {
        Content content = contentRepository.findByIdWithCategory(contentId)
                .orElseThrow(() -> BaseException.of(ContentErrorCode.CONTENT_NOT_FOUND));

        Category category = content.getCategory();
        int totalContentsInCategory = contentRepository.countByCategoryAndIsPremiumFalse(category);

        List<BlockResponse> blocks = new ArrayList<>();

        // BODY 블록 조립
        List<BodyBlockDataInfo> bodyBlocks = parseBodyData(content.getBodyData());
        for (BodyBlockDataInfo bd : bodyBlocks) {
            ContentDetailResponse.BodyBlockResponse body = buildBodyBlock(bd);
            blocks.add(BlockResponse.ofBody(bd.order(), bd.bodyType(), body));
        }

        // SUMMARY 블록 조립
        if (content.getSummaryContent() != null) {
            blocks.add(BlockResponse.ofSummary(ContentStage.SUMMARY_BLOCK_ORDER, content.getSummaryContent()));
        }

        // QUESTION 블록 조립
        List<ContentQuestion> questions = contentQuestionRepository.findByContent(content);
        for (ContentQuestion q : questions) {
            String stage = q.getContentStage().name();
            int order = q.getContentStage().getBlockOrder();
            List<OptionResponse> options = buildOptions(q);
            blocks.add(BlockResponse.ofQuestion(order, q.getId(), stage,
                    q.getQuestionType().name(), q.getQuestionBody(), options));
        }

        blocks.sort(Comparator.comparingInt(BlockResponse::order));

        return new ContentDetailResponse(
                content.getId(),
                category.getId(),
                category.getCategoryName(),
                content.getTitle(),
                content.getSource(),
                content.getReferenceDate(),
                content.getDisplayOrder(),
                totalContentsInCategory,
                blocks
        );
    }

    private List<BodyBlockDataInfo> parseBodyData(String bodyData) {
        if (bodyData == null || bodyData.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(bodyData, new TypeReference<>() {});
        } catch (Exception e) {
            log.warn("body_data JSON 파싱 실패: {}", e.getMessage());
            return List.of();
        }
    }

    private ContentDetailResponse.BodyBlockResponse buildBodyBlock(BodyBlockDataInfo bd) {
        BodyType type = BodyType.valueOf(bd.bodyType());

        return new ContentDetailResponse.BodyBlockResponse(
                bd.title(),
                type.includes(BodyType.BodyField.DESC) ? bd.description() : null,
                type.includes(BodyType.BodyField.ADD_DESC) ? bd.additionalDescription() : null,
                type.includes(BodyType.BodyField.IMG) ? bd.imageUrl() : null,
                type.includes(BodyType.BodyField.TABLE_IMG) ? bd.tableImageUrl() : null
        );
    }

    private List<OptionResponse> buildOptions(ContentQuestion q) {
        return q.getOptions().stream()
                .map(opt -> new OptionResponse(opt.getKey(), opt.getValue()))
                .toList();
    }

    private List<CategoryDetailResponse.PremiumContentSummary> buildPremiumSummaries(List<Content> premiumContents) {
        return premiumContents.stream()
                .map(content -> new CategoryDetailResponse.PremiumContentSummary(
                        content.getId(),
                        content.getTitle()
                ))
                .toList();
    }
}

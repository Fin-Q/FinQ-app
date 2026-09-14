package com.swyp.FinQ.content.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swyp.FinQ.content.domain.Category;
import com.swyp.FinQ.content.domain.CategoryCode;
import com.swyp.FinQ.content.domain.CompletionStatus;
import com.swyp.FinQ.content.domain.Content;
import com.swyp.FinQ.content.domain.ContentQuestion;
import com.swyp.FinQ.content.dto.info.BodyBlockDataInfo;

import com.swyp.FinQ.content.dto.res.CategoryDetailResponse;
import com.swyp.FinQ.content.dto.res.ContentDetailResponse;
import com.swyp.FinQ.content.dto.res.ContentDetailResponse.BlockResponse;
import com.swyp.FinQ.content.dto.res.ContentDetailResponse.BoxItemResponse;
import com.swyp.FinQ.content.dto.res.ContentDetailResponse.ContentItemResponse;
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

import java.util.Arrays;
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
        List<CategoryDetailResponse.PremiumContentSummary> premiumSummaries = buildPremiumSummaries(premiumContents, completedContentIds);

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
                .map(content -> {
                    String title = null;
                    String description = null;
                    List<BodyBlockDataInfo> bodyBlocks = parseBodyData(content.getBodyData());
                    if (!bodyBlocks.isEmpty()) {
                        title = bodyBlocks.get(0).title();
                        List<BodyBlockDataInfo.ContentItem> items = bodyBlocks.get(0).content();
                        if (items != null && !items.isEmpty()) {
                            description = items.get(0).text();
                        }
                    }
                    return new CategoryDetailResponse.ContentSummary(
                            content.getId(),
                            content.getContentCode(),
                            Arrays.asList(content.getTitle().split("·")),
                            title,
                            description,
                            CompletionStatus.of(completedContentIds.contains(content.getId())).name(),
                            content.getDisplayOrder()
                    );
                })
                .toList();
    }

    public ContentDetailResponse getContentDetail(Long contentId, Long userId) {
        Content content = contentRepository.findByIdWithCategory(contentId)
                .orElseThrow(() -> BaseException.of(ContentErrorCode.CONTENT_NOT_FOUND));

        Category category = content.getCategory();
        List<Content> freeContents = contentRepository.findByCategoryOrderByDisplayOrder(category)
                .stream().filter(c -> !c.isPremium()).toList();
        int totalContentsInCategory = freeContents.size();
        int contentOrder = 1;
        for (int i = 0; i < freeContents.size(); i++) {
            if (freeContents.get(i).getId().equals(content.getId())) {
                contentOrder = i + 1;
                break;
            }
        }

        // BODY 블록 조립
        List<BodyBlockDataInfo> bodyBlocks = parseBodyData(content.getBodyData());
        List<BlockResponse> bodyBlockResponses = new ArrayList<>();
        for (BodyBlockDataInfo bd : bodyBlocks) {
            List<ContentItemResponse> contentItems = mapContentItems(bd.content());
            bodyBlockResponses.add(BlockResponse.ofBody(bd.order(), bd.title(), contentItems));
        }
        bodyBlockResponses.sort(Comparator.comparingInt(BlockResponse::order));

        // QUESTION 블록을 afterPageOrder 기준으로 매핑
        List<ContentQuestion> questions = contentQuestionRepository.findByContent(content);
        Map<Integer, List<BlockResponse>> questionsByPageOrder = new java.util.LinkedHashMap<>();
        List<BlockResponse> unmappedQuestions = new ArrayList<>();

        for (ContentQuestion q : questions) {
            List<OptionResponse> options = buildOptions(q);
            BlockResponse questionBlock = BlockResponse.ofQuestion(q.getId(), q.getContentStage().name(),
                    q.getQuestionType().name(), q.getQuestionBody(), options);

            int afterPage = q.getAfterPageOrder();
            if (afterPage > 0) {
                questionsByPageOrder.computeIfAbsent(afterPage, k -> new ArrayList<>()).add(questionBlock);
            } else {
                unmappedQuestions.add(questionBlock);
            }
        }

        // BODY와 QUESTION을 올바른 순서로 조합
        List<BlockResponse> blocks = new ArrayList<>();
        for (BlockResponse body : bodyBlockResponses) {
            blocks.add(body);

            List<BlockResponse> questionsAfterPage = questionsByPageOrder.get(body.order());
            if (questionsAfterPage != null) {
                blocks.addAll(questionsAfterPage);
            }
        }

        // 매핑되지 않은 QUESTION 블록 추가
        blocks.addAll(unmappedQuestions);

        return new ContentDetailResponse(
                content.getId(),
                category.getId(),
                category.getCategoryName(),
                content.getTitle(),
                content.getSource(),
                content.getReferenceDate(),
                contentOrder,
                totalContentsInCategory,
                blocks
        );
    }

    private List<ContentItemResponse> mapContentItems(List<BodyBlockDataInfo.ContentItem> items) {
        if (items == null) {
            return List.of();
        }
        return items.stream()
                .map(item -> new ContentItemResponse(
                        item.type(),
                        item.text(),
                        item.items() != null
                                ? item.items().stream()
                                        .map(box -> new BoxItemResponse(box.title(), box.text()))
                                        .toList()
                                : null,
                        item.imageUrl()
                ))
                .toList();
    }

    private List<BodyBlockDataInfo> parseBodyData(String bodyData) {
        if (bodyData == null || bodyData.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(bodyData, new TypeReference<>() {});
        } catch (Exception e) {
            log.error("body_data JSON 파싱 실패: {}", e.getMessage());
            throw BaseException.of(ContentErrorCode.BODY_DATA_PARSE_FAILED);
        }
    }

    private List<OptionResponse> buildOptions(ContentQuestion q) {
        return q.getOptions().stream()
                .map(opt -> new OptionResponse(opt.getKey(), opt.getValue()))
                .toList();
    }

    private List<CategoryDetailResponse.PremiumContentSummary> buildPremiumSummaries(
            List<Content> premiumContents, Set<Long> completedContentIds) {
        return premiumContents.stream()
                .map(content -> {
                    String title = null;
                    String description = null;
                    List<BodyBlockDataInfo> bodyBlocks = parseBodyData(content.getBodyData());
                    if (!bodyBlocks.isEmpty()) {
                        title = bodyBlocks.get(0).title();
                        List<BodyBlockDataInfo.ContentItem> items = bodyBlocks.get(0).content();
                        if (items != null && !items.isEmpty()) {
                            description = items.get(0).text();
                        }
                    }
                    return new CategoryDetailResponse.PremiumContentSummary(
                            content.getId(),
                            Arrays.asList(content.getTitle().split("·")),
                            title,
                            description,
                            CompletionStatus.of(completedContentIds.contains(content.getId())).name()
                    );
                })
                .toList();
    }
}

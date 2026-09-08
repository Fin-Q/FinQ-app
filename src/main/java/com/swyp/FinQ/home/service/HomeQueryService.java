package com.swyp.FinQ.home.service;

import com.swyp.FinQ.content.domain.CompletionStatus;
import com.swyp.FinQ.content.domain.Content;
import com.swyp.FinQ.content.repository.ContentRepository;
import com.swyp.FinQ.global.exception.BaseException;
import com.swyp.FinQ.home.dto.res.HomeResponse;
import com.swyp.FinQ.learning.repository.UserContentCompletionRepository;
import com.swyp.FinQ.reward.domain.Level;
import com.swyp.FinQ.streak.service.StreakQueryService;
import com.swyp.FinQ.user.domain.User;
import com.swyp.FinQ.user.domain.UserInterest;
import com.swyp.FinQ.user.exception.UserErrorCode;
import com.swyp.FinQ.user.repository.UserInterestRepository;
import com.swyp.FinQ.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HomeQueryService {

    private static final int QUESTION_CARD_COUNT = 3;
    private static final int RANDOM_POOL_SIZE = 10;

    private final UserRepository userRepository;
    private final UserInterestRepository userInterestRepository;
    private final ContentRepository contentRepository;
    private final UserContentCompletionRepository userContentCompletionRepository;
    private final StreakQueryService streakQueryService;

    @Transactional
    public HomeResponse getHome(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> BaseException.of(UserErrorCode.USER_NOT_FOUND));

        Level level = Level.from(user.getTotalXp());
        List<HomeResponse.QuestionCard> questions = getOrBuildQuestionCards(user);

        return new HomeResponse(
                user.getNickname(),
                level.getValue(),
                level.getValue(),
                user.getTotalXp(),
                streakQueryService.getCurrentStreak(userId),
                questions
        );
    }

    private List<HomeResponse.QuestionCard> getOrBuildQuestionCards(User user) {
        LocalDate today = LocalDate.now();

        // 같은 날이면 캐시된 질문 반환
        if (today.equals(user.getHomeQuestionDate()) && user.getHomeQuestionIds() != null) {
            List<Long> cachedIds = parseCachedIds(user.getHomeQuestionIds());
            if (!cachedIds.isEmpty()) {
                List<Content> contents = contentRepository.findAllByIdWithCategory(cachedIds);
                if (!contents.isEmpty()) {
                    return toQuestionCards(user.getId(), contents, cachedIds);
                }
            }
        }

        // 새로운 질문 카드 생성
        List<Content> newContents = buildQuestionCards(user);

        // 캐시 업데이트
        String ids = newContents.stream()
                .map(c -> String.valueOf(c.getId()))
                .collect(Collectors.joining(","));
        user.updateHomeQuestionCache(today, ids);

        Set<Long> completedIds = newContents.isEmpty()
                ? Set.of()
                : userContentCompletionRepository.findCompletedContentIdsByUserIdAndContentIn(user.getId(), newContents);

        return newContents.stream()
                .map(content -> new HomeResponse.QuestionCard(
                        content.getContentCode(),
                        content.getCategory().getCategoryCode().name(),
                        content.getCategory().getCategoryName(),
                        content.getTitle(),
                        completedIds.contains(content.getId())
                                ? CompletionStatus.COMPLETED.name()
                                : CompletionStatus.INCOMPLETE.name()
                ))
                .toList();
    }

    private List<Content> buildQuestionCards(User user) {
        List<UserInterest> interests = userInterestRepository.findAllWithCategoryByUserId(user.getId());
        List<Long> categoryIds = interests.stream()
                .map(i -> i.getCategory().getId())
                .toList();

        if (categoryIds.isEmpty()) {
            return List.of();
        }

        // 이전에 출제된 질문 ID 목록 (제외 대상)
        List<Long> previousIds = user.getHomeQuestionIds() != null
                ? parseCachedIds(user.getHomeQuestionIds())
                : List.of();

        if (categoryIds.size() == 1) {
            return pickContentsForSingleCategory(user.getId(), categoryIds.get(0), previousIds);
        }

        // 카테고리 2개: 홀짝 날짜 기반 2:1 교차 출제
        return pickContentsForTwoCategories(user.getId(), categoryIds.get(0), categoryIds.get(1), previousIds);
    }

    private List<Content> pickContentsForSingleCategory(Long userId, Long categoryId, List<Long> excludeIds) {
        List<Content> pool = findIncompleteContents(userId, categoryId, excludeIds, RANDOM_POOL_SIZE);

        if (pool.size() < QUESTION_CARD_COUNT) {
            List<Long> allExcludeIds = mergeExcludeIds(excludeIds, pool);
            List<Content> fallback = findAllContents(categoryId, allExcludeIds, RANDOM_POOL_SIZE);
            pool = new ArrayList<>(pool);
            pool.addAll(fallback);
        }

        // 그래도 부족하면 제외 조건 없이 조회 (폴백)
        if (pool.isEmpty()) {
            pool = contentRepository.findContentsByCategoryNoExclude(
                    categoryId, PageRequest.of(0, RANDOM_POOL_SIZE));
        }

        return pickRandom(pool, QUESTION_CARD_COUNT);
    }

    private List<Content> pickContentsForTwoCategories(Long userId, Long cat1, Long cat2, List<Long> excludeIds) {
        int dayOfYear = LocalDate.now().getDayOfYear();
        boolean isOdd = dayOfYear % 2 == 1;

        // 홀수일: cat1 2장 + cat2 1장, 짝수일: cat2 2장 + cat1 1장
        Long primaryCat = isOdd ? cat1 : cat2;
        Long secondaryCat = isOdd ? cat2 : cat1;

        List<Content> primaryContents = pickContentsForCategory(userId, primaryCat, excludeIds, 2);
        List<Long> usedIds = mergeExcludeIds(excludeIds, primaryContents);

        List<Content> secondaryContents = pickContentsForCategory(userId, secondaryCat, usedIds, 1);

        List<Content> result = new ArrayList<>(primaryContents);
        result.addAll(secondaryContents);

        // 부족한 경우 다른 카테고리에서 채움
        if (result.size() < QUESTION_CARD_COUNT) {
            int remaining = QUESTION_CARD_COUNT - result.size();
            List<Long> allUsedIds = mergeExcludeIds(excludeIds, result);

            List<Content> fallbackPrimary = pickContentsForCategory(userId, primaryCat, allUsedIds, remaining);
            result.addAll(fallbackPrimary);

            if (result.size() < QUESTION_CARD_COUNT) {
                remaining = QUESTION_CARD_COUNT - result.size();
                allUsedIds = mergeExcludeIds(allUsedIds, fallbackPrimary);

                List<Content> fallbackSecondary = pickContentsForCategory(userId, secondaryCat, allUsedIds, remaining);
                result.addAll(fallbackSecondary);
            }
        }

        return result;
    }

    private List<Content> pickContentsForCategory(Long userId, Long categoryId, List<Long> excludeIds, int count) {
        // 미완료 콘텐츠 우선 - 풀에서 랜덤 선택
        List<Content> pool = findIncompleteContents(userId, categoryId, excludeIds, RANDOM_POOL_SIZE);
        List<Content> picked = pickRandom(pool, count);

        if (picked.size() < count) {
            int remaining = count - picked.size();
            List<Long> allExcludeIds = mergeExcludeIds(excludeIds, picked);

            List<Content> fallbackPool = findAllContents(categoryId, allExcludeIds, RANDOM_POOL_SIZE);
            picked = new ArrayList<>(picked);
            picked.addAll(pickRandom(fallbackPool, remaining));
        }

        return picked;
    }

    private List<Content> findIncompleteContents(Long userId, Long categoryId, List<Long> excludeIds, int limit) {
        if (excludeIds.isEmpty()) {
            return contentRepository.findIncompleteContentsByCategoryNoExclude(
                    userId, categoryId, PageRequest.of(0, limit));
        }
        return contentRepository.findIncompleteContentsByCategory(
                userId, categoryId, excludeIds, PageRequest.of(0, limit));
    }

    private List<Content> findAllContents(Long categoryId, List<Long> excludeIds, int limit) {
        if (excludeIds.isEmpty()) {
            return contentRepository.findContentsByCategoryNoExclude(
                    categoryId, PageRequest.of(0, limit));
        }
        return contentRepository.findContentsByCategory(
                categoryId, excludeIds, PageRequest.of(0, limit));
    }

    private List<Long> mergeExcludeIds(List<Long> existing, List<Content> newContents) {
        List<Long> merged = new ArrayList<>(existing);
        merged.addAll(newContents.stream().map(Content::getId).toList());
        return merged;
    }

    private List<Content> pickRandom(List<Content> pool, int count) {
        if (pool.size() <= count) {
            return new ArrayList<>(pool);
        }
        List<Content> shuffled = new ArrayList<>(pool);
        Collections.shuffle(shuffled);
        return shuffled.subList(0, count);
    }

    private List<HomeResponse.QuestionCard> toQuestionCards(Long userId, List<Content> contents, List<Long> orderedIds) {
        java.util.Map<Long, Content> contentMap = contents.stream()
                .collect(Collectors.toMap(Content::getId, c -> c));

        List<Content> ordered = orderedIds.stream()
                .filter(contentMap::containsKey)
                .map(contentMap::get)
                .toList();

        Set<Long> completedIds = userContentCompletionRepository
                .findCompletedContentIdsByUserIdAndContentIn(userId, new ArrayList<>(ordered));

        return ordered.stream()
                .map(content -> new HomeResponse.QuestionCard(
                        content.getContentCode(),
                        content.getCategory().getCategoryCode().name(),
                        content.getCategory().getCategoryName(),
                        content.getTitle(),
                        completedIds.contains(content.getId())
                                ? CompletionStatus.COMPLETED.name()
                                : CompletionStatus.INCOMPLETE.name()
                ))
                .toList();
    }

    private List<Long> parseCachedIds(String ids) {
        if (ids == null || ids.isBlank()) {
            return List.of();
        }
        return Arrays.stream(ids.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::valueOf)
                .toList();
    }
}
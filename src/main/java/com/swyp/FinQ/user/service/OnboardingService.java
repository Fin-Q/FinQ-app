package com.swyp.FinQ.user.service;

import com.swyp.FinQ.content.domain.Category;
import com.swyp.FinQ.content.exception.ContentErrorCode;
import com.swyp.FinQ.content.repository.CategoryRepository;
import com.swyp.FinQ.global.exception.BaseException;
import com.swyp.FinQ.user.domain.OnboardingStatus;
import com.swyp.FinQ.user.domain.User;
import com.swyp.FinQ.user.domain.UserInterest;
import com.swyp.FinQ.user.dto.req.InterestSelectionRequest;
import com.swyp.FinQ.user.dto.res.OnboardingResponse;
import com.swyp.FinQ.user.exception.UserErrorCode;
import com.swyp.FinQ.user.repository.UserInterestRepository;
import com.swyp.FinQ.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class OnboardingService {

    private static final ZoneId SERVICE_ZONE_ID = ZoneId.of("Asia/Seoul");

    private final UserRepository userRepository;
    private final UserInterestRepository userInterestRepository;
    private final CategoryRepository categoryRepository;
    private final Clock clock;

    @Transactional(readOnly = true)
    public OnboardingResponse getOnboarding(Long userId) {
        User user = getUser(userId);
        return OnboardingResponse.of(user, userInterestRepository.findAllWithCategoryByUserId(userId));
    }

    @Transactional
    public OnboardingResponse selectInterests(Long userId, InterestSelectionRequest request) {
        User user = getUser(userId);
        validateInitialSelection(user, request.interestTopicIds());

        List<UserInterest> interests = createInterests(user, resolveCategories(request.interestTopicIds()));
        userInterestRepository.saveAll(interests);
        user.moveToCharacterGuide();

        return OnboardingResponse.of(user, interests);
    }

    @Transactional
    public OnboardingResponse updateInterests(Long userId, InterestSelectionRequest request) {
        User user = getUser(userId);
        if (user.getOnboardingStatus() == OnboardingStatus.INTEREST_SELECTION
                || !userInterestRepository.existsByUserId(userId)) {
            throw BaseException.of(UserErrorCode.INTEREST_NOT_SELECTED);
        }

        List<Category> categories = resolveCategories(request.interestTopicIds());
        userInterestRepository.deleteAllByUserId(userId);
        List<UserInterest> interests = createInterests(user, categories);
        userInterestRepository.saveAll(interests);

        // 관심 주제 변경 시 홈 질문 캐시 초기화
        user.updateHomeQuestionCache(null, null);

        return OnboardingResponse.of(user, interests);
    }

    @Transactional
    public OnboardingResponse completeOnboarding(Long userId) {
        User user = getUser(userId);
        if (user.getOnboardingStatus() == OnboardingStatus.INTEREST_SELECTION) {
            throw BaseException.of(UserErrorCode.ONBOARDING_INTEREST_REQUIRED);
        }
        if (user.getOnboardingStatus() == OnboardingStatus.CHARACTER_GUIDE) {
            user.completeOnboarding(LocalDateTime.ofInstant(clock.instant(), SERVICE_ZONE_ID));
        }

        return OnboardingResponse.of(user, userInterestRepository.findAllWithCategoryByUserId(userId));
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> BaseException.of(UserErrorCode.USER_NOT_FOUND));
    }

    private void validateInitialSelection(User user, List<Long> interestTopicIds) {
        if (user.getOnboardingStatus() != OnboardingStatus.INTEREST_SELECTION
                || userInterestRepository.existsByUserId(user.getId())) {
            throw BaseException.of(UserErrorCode.INTEREST_ALREADY_SELECTED);
        }
        if (new LinkedHashSet<>(interestTopicIds).size() != interestTopicIds.size()) {
            throw BaseException.of(UserErrorCode.DUPLICATE_INTEREST_CATEGORY);
        }
    }

    private List<Category> resolveCategories(List<Long> requestedIds) {
        Set<Long> interestTopicIds = new LinkedHashSet<>(requestedIds);
        if (interestTopicIds.size() != requestedIds.size()) {
            throw BaseException.of(UserErrorCode.DUPLICATE_INTEREST_CATEGORY);
        }

        List<Category> categories = categoryRepository.findAllById(interestTopicIds).stream()
                .sorted((left, right) -> left.getDisplayOrder().compareTo(right.getDisplayOrder()))
                .toList();
        if (categories.size() != interestTopicIds.size()) {
            throw BaseException.of(ContentErrorCode.CATEGORY_NOT_FOUND);
        }
        return categories;
    }

    private List<UserInterest> createInterests(User user, List<Category> categories) {
        return categories.stream()
                .map(category -> UserInterest.builder()
                        .user(user)
                        .category(category)
                        .build())
                .toList();
    }
}

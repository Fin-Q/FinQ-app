package com.swyp.FinQ.user.service;

import com.swyp.FinQ.content.domain.Category;
import com.swyp.FinQ.content.domain.CategoryCode;
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

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class OnboardingService {

    private final UserRepository userRepository;
    private final UserInterestRepository userInterestRepository;
    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public OnboardingResponse getOnboarding(Long userId) {
        User user = getUser(userId);
        return OnboardingResponse.of(user, userInterestRepository.findAllWithCategoryByUserId(userId));
    }

    @Transactional
    public OnboardingResponse selectInterests(Long userId, InterestSelectionRequest request) {
        User user = getUser(userId);
        validateInitialSelection(user, request.categoryCodes());

        Set<CategoryCode> categoryCodes = new LinkedHashSet<>(request.categoryCodes());
        List<Category> categories = categoryRepository.findAllByCategoryCodeIn(categoryCodes).stream()
                .sorted((left, right) -> left.getDisplayOrder().compareTo(right.getDisplayOrder()))
                .toList();
        if (categories.size() != categoryCodes.size()) {
            throw BaseException.of(ContentErrorCode.CATEGORY_NOT_FOUND);
        }

        List<UserInterest> interests = categories.stream()
                .map(category -> UserInterest.builder()
                        .user(user)
                        .category(category)
                        .build())
                .toList();
        userInterestRepository.saveAll(interests);
        user.moveToCharacterGuide();

        return OnboardingResponse.of(user, interests);
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> BaseException.of(UserErrorCode.USER_NOT_FOUND));
    }

    private void validateInitialSelection(User user, List<CategoryCode> categoryCodes) {
        if (user.getOnboardingStatus() != OnboardingStatus.INTEREST_SECTION
                || userInterestRepository.existsByUserId(user.getId())) {
            throw BaseException.of(UserErrorCode.INTEREST_ALREADY_SELECTED);
        }
        if (new LinkedHashSet<>(categoryCodes).size() != categoryCodes.size()) {
            throw BaseException.of(UserErrorCode.DUPLICATE_INTEREST_CATEGORY);
        }
    }
}

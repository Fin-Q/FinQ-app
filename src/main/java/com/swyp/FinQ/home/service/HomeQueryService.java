package com.swyp.FinQ.home.service;

import com.swyp.FinQ.content.domain.CompletionStatus;
import com.swyp.FinQ.content.domain.Content;
import com.swyp.FinQ.content.repository.ContentRepository;
import com.swyp.FinQ.global.exception.BaseException;
import com.swyp.FinQ.home.dto.res.HomeResponse;
import com.swyp.FinQ.reward.domain.Level;
import com.swyp.FinQ.user.domain.User;
import com.swyp.FinQ.user.exception.UserErrorCode;
import com.swyp.FinQ.user.repository.UserInterestRepository;
import com.swyp.FinQ.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HomeQueryService {

    private static final int QUESTION_CARD_COUNT = 3;

    private final UserRepository userRepository;
    private final UserInterestRepository userInterestRepository;
    private final ContentRepository contentRepository;

    public HomeResponse getHome(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> BaseException.of(UserErrorCode.USER_NOT_FOUND));

        Level level = Level.from(user.getTotalXp());
        List<HomeResponse.QuestionCard> questions = buildQuestionCards(userId);

        return new HomeResponse(
                user.getNickname(),
                level.getValue(),
                level.getValue(),
                user.getTotalXp(),
                user.getCurrentStreak(),
                questions
        );
    }

    private List<HomeResponse.QuestionCard> buildQuestionCards(Long userId) {
        List<Long> interestCategoryIds = userInterestRepository.findAllWithCategoryByUserId(userId).stream()
                .map(interest -> interest.getCategory().getId())
                .toList();

        if (interestCategoryIds.isEmpty()) {
            return List.of();
        }

        // 미완료 콘텐츠 우선
        List<Content> incomplete = contentRepository.findIncompleteContentsByCategories(
                userId, interestCategoryIds, PageRequest.of(0, QUESTION_CARD_COUNT));

        List<Content> result = new ArrayList<>(incomplete);

        // 미완료 콘텐츠가 3개 미만이면 완료된 콘텐츠로 채움
        if (result.size() < QUESTION_CARD_COUNT) {
            int remaining = QUESTION_CARD_COUNT - result.size();
            List<Content> completed = contentRepository.findCompletedContentsByCategories(
                    userId, interestCategoryIds, PageRequest.of(0, remaining));
            result.addAll(completed);
        }

        List<Long> incompleteIds = incomplete.stream().map(Content::getId).toList();

        return result.stream()
                .map(content -> new HomeResponse.QuestionCard(
                        content.getContentCode(),
                        content.getCategory().getCategoryCode().name(),
                        content.getCategory().getCategoryName(),
                        content.getTitle(),
                        incompleteIds.contains(content.getId())
                                ? CompletionStatus.INCOMPLETE.name()
                                : CompletionStatus.COMPLETED.name()
                ))
                .toList();
    }
}
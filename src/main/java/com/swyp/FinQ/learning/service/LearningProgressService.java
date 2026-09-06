package com.swyp.FinQ.learning.service;

import com.swyp.FinQ.content.domain.Content;
import com.swyp.FinQ.learning.repository.UserCategoryCompletionRepository;
import com.swyp.FinQ.learning.repository.UserContentCompletionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LearningProgressService {

    private final UserContentCompletionRepository userContentCompletionRepository;
    private final UserCategoryCompletionRepository userCategoryCompletionRepository;

    public Set<Long> getCompletedContentIds(Long userId, List<Content> contents) {
        return userContentCompletionRepository.findCompletedContentIdsByUserIdAndContentIn(userId, contents);
    }

    public Set<Long> getCompletedCategoryIds(Long userId) {
        return userCategoryCompletionRepository.findCompletedCategoryIdsByUserId(userId);
    }

    public boolean isCategoryCompleted(Long userId, Long categoryId) {
        return userCategoryCompletionRepository.existsByUserIdAndCategoryId(userId, categoryId);
    }

    public int calculateProgressRate(int completed, int total) {
        return total > 0 ? (int) ((double) completed / total * 100) : 0;
    }
}

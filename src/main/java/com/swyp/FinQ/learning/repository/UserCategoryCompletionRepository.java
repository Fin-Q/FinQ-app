package com.swyp.FinQ.learning.repository;

import com.swyp.FinQ.learning.domain.UserCategoryCompletion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserCategoryCompletionRepository extends JpaRepository<UserCategoryCompletion, Long> {

    Optional<UserCategoryCompletion> findByUserIdAndCategoryId(Long userId, Long categoryId);

    List<UserCategoryCompletion> findByUserId(Long userId);

    boolean existsByUserIdAndCategoryId(Long userId, Long categoryId);
}
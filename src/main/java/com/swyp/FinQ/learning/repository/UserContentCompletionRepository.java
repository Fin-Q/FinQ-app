package com.swyp.FinQ.learning.repository;

import com.swyp.FinQ.learning.domain.UserContentCompletion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserContentCompletionRepository extends JpaRepository<UserContentCompletion, Long> {

    Optional<UserContentCompletion> findByUserIdAndContentId(Long userId, Long contentId);

    List<UserContentCompletion> findByUserId(Long userId);

    boolean existsByUserIdAndContentId(Long userId, Long contentId);
}

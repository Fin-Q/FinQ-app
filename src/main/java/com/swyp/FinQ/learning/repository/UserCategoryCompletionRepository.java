package com.swyp.FinQ.learning.repository;

import com.swyp.FinQ.learning.domain.UserCategoryCompletion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;

public interface UserCategoryCompletionRepository extends JpaRepository<UserCategoryCompletion, Long> {

    boolean existsByUserIdAndCategoryId(Long userId, Long categoryId);

    @Query("SELECT ucc.category.id FROM UserCategoryCompletion ucc WHERE ucc.user.id = :userId")
    Set<Long> findCompletedCategoryIdsByUserId(@Param("userId") Long userId);
}
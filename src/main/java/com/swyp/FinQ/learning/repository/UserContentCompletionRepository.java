package com.swyp.FinQ.learning.repository;

import com.swyp.FinQ.content.domain.Content;
import com.swyp.FinQ.learning.domain.UserContentCompletion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface UserContentCompletionRepository extends JpaRepository<UserContentCompletion, Long> {

    boolean existsByUserIdAndContentId(Long userId, Long contentId);

    @Query("SELECT ucc.content.id FROM UserContentCompletion ucc WHERE ucc.user.id = :userId AND ucc.content IN :contents")
    Set<Long> findCompletedContentIdsByUserIdAndContentIn(@Param("userId") Long userId, @Param("contents") List<Content> contents);
}

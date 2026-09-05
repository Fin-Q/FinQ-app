package com.swyp.FinQ.content.repository;

import com.swyp.FinQ.content.domain.Category;
import com.swyp.FinQ.content.domain.Content;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ContentRepository extends JpaRepository<Content, Long> {

    List<Content> findByCategoryOrderByDisplayOrder(Category category);

    @Query("SELECT c.category.id AS categoryId, COUNT(c) AS contentCount " +
            "FROM Content c " +
            "WHERE c.isPremium = false " +
            "GROUP BY c.category.id")
    List<CategoryContentCount> countContentPerCategory();

    @Query("SELECT c.category.id AS categoryId, COUNT(c) AS contentCount " +
            "FROM Content c " +
            "JOIN UserContentCompletion ucc ON ucc.content = c AND ucc.userId = :userId " +
            "WHERE c.isPremium = false " +
            "GROUP BY c.category.id")
    List<CategoryContentCount> countCompletedContentPerCategory(@Param("userId") Long userId);

    int countByCategoryAndIsPremiumFalse(Category category);

    @Query("SELECT c FROM Content c JOIN FETCH c.category WHERE c.id = :contentId")
    Optional<Content> findByIdWithCategory(@Param("contentId") Long contentId);
}
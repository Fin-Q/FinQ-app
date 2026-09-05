package com.swyp.FinQ.content.repository;

import com.swyp.FinQ.content.domain.Category;
import com.swyp.FinQ.content.domain.Content;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ContentRepository extends JpaRepository<Content, Long> {

    Optional<Content> findByContentCode(String contentCode);

    List<Content> findByCategoryOrderByDisplayOrder(Category category);
}
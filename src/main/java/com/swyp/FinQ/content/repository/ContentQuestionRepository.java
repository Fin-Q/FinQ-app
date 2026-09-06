package com.swyp.FinQ.content.repository;

import com.swyp.FinQ.content.domain.Content;
import com.swyp.FinQ.content.domain.ContentQuestion;
import com.swyp.FinQ.content.domain.ContentStage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ContentQuestionRepository extends JpaRepository<ContentQuestion, Long> {

    Optional<ContentQuestion> findByContentAndContentStage(Content content, ContentStage contentStage);

    List<ContentQuestion> findByContent(Content content);
}
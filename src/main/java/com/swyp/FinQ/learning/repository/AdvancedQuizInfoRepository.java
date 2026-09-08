package com.swyp.FinQ.learning.repository;

import com.swyp.FinQ.learning.domain.AdvancedQuizInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdvancedQuizInfoRepository extends JpaRepository<AdvancedQuizInfo, Long> {

    Optional<AdvancedQuizInfo> findByCategoryId(Long categoryId);
}
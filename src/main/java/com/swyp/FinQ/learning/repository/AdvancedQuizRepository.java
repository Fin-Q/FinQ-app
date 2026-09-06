package com.swyp.FinQ.learning.repository;

import com.swyp.FinQ.learning.domain.AdvancedQuiz;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdvancedQuizRepository extends JpaRepository<AdvancedQuiz, Long> {

    List<AdvancedQuiz> findByCategoryIdOrderByQuizOrder(Long categoryId);
}

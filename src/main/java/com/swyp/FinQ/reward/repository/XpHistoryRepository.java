package com.swyp.FinQ.reward.repository;

import com.swyp.FinQ.reward.domain.XpHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface XpHistoryRepository extends JpaRepository<XpHistory, Long> {

    List<XpHistory> findByUserIdOrderByCreatedAtDesc(Long userId);
}
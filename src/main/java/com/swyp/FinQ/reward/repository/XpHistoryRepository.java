package com.swyp.FinQ.reward.repository;

import com.swyp.FinQ.reward.domain.XpHistory;
import com.swyp.FinQ.reward.domain.XpType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface XpHistoryRepository extends JpaRepository<XpHistory, Long> {

    boolean existsByUserIdAndXpTypeAndReferenceId(Long userId, XpType xpType, String referenceId);

    @Query("SELECT COALESCE(SUM(x.xpAmount), 0) FROM XpHistory x WHERE x.user.id = :userId")
    int calculateTotalXpByUserId(@Param("userId") Long userId);
}
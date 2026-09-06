package com.swyp.FinQ.reward.repository;

import com.swyp.FinQ.reward.domain.XpHistory;
import com.swyp.FinQ.reward.domain.XpType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface XpHistoryRepository extends JpaRepository<XpHistory, Long> {

    List<XpHistory> findByUserIdOrderByCreatedAtDesc(Long userId);

    boolean existsByUserIdAndXpTypeAndReferenceId(Long userId, XpType xpType, String referenceId);

    // TODO: User 도메인 구현 후 서비스에서의 직접 호출 제거, 정합성 검증용으로만 유지
    @Query("SELECT COALESCE(SUM(x.xpAmount), 0) FROM XpHistory x WHERE x.userId = :userId")
    int calculateTotalXpByUserId(@Param("userId") Long userId);
}
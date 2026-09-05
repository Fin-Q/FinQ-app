package com.swyp.FinQ.reward.service;

import com.swyp.FinQ.reward.domain.Level;
import com.swyp.FinQ.reward.repository.XpHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class XpQueryService {

    private final XpHistoryRepository xpHistoryRepository;

    // TODO: User 도메인 구현 후 user.getTotalXp()로 전환
    public int getTotalXp(Long userId) {
        return xpHistoryRepository.calculateTotalXpByUserId(userId);
    }

    public Level getLevel(Long userId) {
        return Level.from(getTotalXp(userId));
    }
}

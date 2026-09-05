package com.swyp.FinQ.reward.service;

import com.swyp.FinQ.reward.domain.Level;
import com.swyp.FinQ.reward.domain.XpHistory;
import com.swyp.FinQ.reward.domain.XpType;
import com.swyp.FinQ.reward.dto.info.XpResultInfo;
import com.swyp.FinQ.reward.repository.XpHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class XpGrantService {

    private static final int CONTENT_COMPLETE_XP = 10;
    private static final int QUIZ_COMPLETE_XP = 30;

    private final XpHistoryRepository xpHistoryRepository;

    /**
     * 신규 콘텐츠 최초 완료 시 XP 지급 (+10XP)
     */
    @Transactional
    public XpResultInfo grantContentCompletionXp(Long userId, Long contentId) {
        String referenceId = "content:" + contentId;
        return grantXp(userId, CONTENT_COMPLETE_XP, XpType.CONTENT_COMPLETE, referenceId);
    }

    /**
     * 심화퀴즈 최초 통과 시 XP 지급 (+30XP)
     */
    @Transactional
    public XpResultInfo grantQuizCompletionXp(Long userId, Long categoryId) {
        String referenceId = "category:" + categoryId;
        return grantXp(userId, QUIZ_COMPLETE_XP, XpType.QUIZ_COMPLETE, referenceId);
    }

    /**
     * XP 지급 공통 처리: 중복 체크 → 이력 저장 → 레벨업 판정
     * TODO: User 도메인 구현 후 SUM 쿼리 → user.getTotalXp()로 전환, 이력 저장 시 user.addXp() 호출 추가
     */
    private XpResultInfo grantXp(Long userId, int xpAmount, XpType xpType, String referenceId) {
        int totalXp = xpHistoryRepository.calculateTotalXpByUserId(userId);
        Level currentLevel = Level.from(totalXp);

        if (xpHistoryRepository.existsByUserIdAndXpTypeAndReferenceId(userId, xpType, referenceId)) {
            return XpResultInfo.skipped(totalXp, currentLevel);
        }

        XpHistory xpHistory = XpHistory.builder()
                .userId(userId)
                .xpAmount(xpAmount)
                .xpType(xpType)
                .referenceId(referenceId)
                .build();
        xpHistoryRepository.save(xpHistory);

        int newTotalXp = totalXp + xpAmount;
        Level newLevel = Level.from(newTotalXp);

        return XpResultInfo.granted(xpAmount, newTotalXp, currentLevel, newLevel);
    }
}
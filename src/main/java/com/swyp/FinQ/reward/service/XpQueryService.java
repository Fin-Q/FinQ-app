package com.swyp.FinQ.reward.service;

import com.swyp.FinQ.reward.domain.Level;
import com.swyp.FinQ.reward.dto.res.RewardStatusResponse;
import com.swyp.FinQ.user.domain.User;
import com.swyp.FinQ.user.repository.UserRepository;
import com.swyp.FinQ.global.exception.BaseException;
import com.swyp.FinQ.user.exception.UserErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class XpQueryService {

    private final UserRepository userRepository;

    public RewardStatusResponse getRewardStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> BaseException.of(UserErrorCode.USER_NOT_FOUND));
        Level level = Level.from(user.getTotalXp());
        return new RewardStatusResponse(
                user.getTotalXp(),
                level.getValue(),
                level.getValue()
        );
    }
}

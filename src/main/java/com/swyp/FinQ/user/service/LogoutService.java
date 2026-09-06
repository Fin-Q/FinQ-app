package com.swyp.FinQ.user.service;

import com.swyp.FinQ.user.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LogoutService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public void logout(Long userId, String sessionId) {
        refreshTokenRepository.deleteByUser_IdAndSessionId(userId, sessionId);
    }
}

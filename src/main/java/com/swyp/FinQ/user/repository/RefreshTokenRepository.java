package com.swyp.FinQ.user.repository;

import com.swyp.FinQ.user.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    Optional<RefreshToken> findBySessionId(String sessionId);

    void deleteAllByUserId(Long userId);
}

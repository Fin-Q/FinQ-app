package com.swyp.FinQ.user.repository;

import com.swyp.FinQ.user.domain.PasswordResetRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetRequestRepository extends JpaRepository<PasswordResetRequest, Long> {

    Optional<PasswordResetRequest> findByVerificationId(String verificationId);

    Optional<PasswordResetRequest> findByPasswordResetTokenHash(String passwordResetTokenHash);

    Optional<PasswordResetRequest> findTopByUserIdOrderByCreatedAtDesc(Long userId);

    void deleteAllByUserId(Long userId);
}

package com.swyp.FinQ.user.repository;

import com.swyp.FinQ.user.domain.PasswordResetRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.util.Optional;

public interface PasswordResetRequestRepository extends JpaRepository<PasswordResetRequest, Long> {

    Optional<PasswordResetRequest> findByVerificationId(String verificationId);

    Optional<PasswordResetRequest> findByPasswordResetTokenHash(String passwordResetTokenHash);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from PasswordResetRequest r where r.passwordResetTokenHash = :tokenHash")
    Optional<PasswordResetRequest> findForUpdateByPasswordResetTokenHash(@Param("tokenHash") String tokenHash);

    Optional<PasswordResetRequest> findTopByUserIdOrderByCreatedAtDesc(Long userId);

    void deleteAllByUserId(Long userId);
}

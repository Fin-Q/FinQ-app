package com.swyp.FinQ.user.repository;

import com.swyp.FinQ.user.domain.UserAgreement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserAgreementRepository extends JpaRepository<UserAgreement, Long> {

    List<UserAgreement> findAllByUserId(Long userId);

    boolean existsByUserIdAndAgreementCodeAndAgreementVersion(
            Long userId,
            String agreementCode,
            String agreementVersion
    );
}

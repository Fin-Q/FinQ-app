package com.swyp.FinQ.user.repository;

import com.swyp.FinQ.user.domain.SocialAccount;
import com.swyp.FinQ.user.domain.SocialProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SocialAccountRepository extends JpaRepository<SocialAccount, Long> {

    Optional<SocialAccount> findByProviderAndProviderUserId(SocialProvider provider, String providerUserId);

    @Query("""
            SELECT new com.swyp.FinQ.user.repository.SocialAccountUnlinkTarget(
                account.provider,
                account.providerUserId,
                account.encryptedRefreshToken
            )
            FROM SocialAccount account
            WHERE account.user.id = :userId
            ORDER BY account.id
            """)
    List<SocialAccountUnlinkTarget> findAllUnlinkTargetsByUserId(@Param("userId") Long userId);

    boolean existsByUserIdAndProvider(Long userId, SocialProvider provider);
}

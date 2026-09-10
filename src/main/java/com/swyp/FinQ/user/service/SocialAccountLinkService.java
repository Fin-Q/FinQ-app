package com.swyp.FinQ.user.service;

import com.swyp.FinQ.global.exception.BaseException;
import com.swyp.FinQ.user.domain.SocialAccount;
import com.swyp.FinQ.user.domain.SocialProvider;
import com.swyp.FinQ.user.domain.User;
import com.swyp.FinQ.user.dto.res.SocialAccountLinkResponse;
import com.swyp.FinQ.user.exception.UserErrorCode;
import com.swyp.FinQ.user.repository.SocialAccountRepository;
import com.swyp.FinQ.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SocialAccountLinkService {
    private final UserRepository users;
    private final SocialAccountRepository accounts;

    @Transactional
    public SocialAccountLinkResponse link(Long userId, SocialProvider provider, String providerUserId) {
        return link(userId, provider, providerUserId, null);
    }

    @Transactional
    public SocialAccountLinkResponse link(
            Long userId,
            SocialProvider provider,
            String providerUserId,
            String encryptedRefreshToken
    ) {
        User user = users.findForUpdateById(userId)
                .orElseThrow(() -> BaseException.of(UserErrorCode.USER_NOT_FOUND));
        var existing = accounts.findByProviderAndProviderUserId(provider, providerUserId);
        if (existing.isPresent()) {
            if (!existing.get().getUser().getId().equals(userId)) {
                throw BaseException.of(UserErrorCode.SOCIAL_ACCOUNT_LINK_CONFLICT);
            }
            updateEncryptedRefreshToken(existing.get(), encryptedRefreshToken);
            return SocialAccountLinkResponse.from(existing.get());
        }
        if (accounts.existsByUserIdAndProvider(userId, provider)) {
            throw BaseException.of(UserErrorCode.SOCIAL_ACCOUNT_LINK_CONFLICT);
        }
        try {
            SocialAccount account = accounts.saveAndFlush(SocialAccount.builder()
                    .user(user)
                    .provider(provider)
                    .providerUserId(providerUserId)
                    .encryptedRefreshToken(encryptedRefreshToken)
                    .build());
            return SocialAccountLinkResponse.from(account);
        } catch (DataIntegrityViolationException exception) {
            // The unique constraints also protect competing links/logins across different users.
            throw BaseException.of(UserErrorCode.SOCIAL_ACCOUNT_LINK_CONFLICT);
        }
    }

    private void updateEncryptedRefreshToken(SocialAccount account, String encryptedRefreshToken) {
        if (encryptedRefreshToken != null) {
            account.updateEncryptedRefreshToken(encryptedRefreshToken);
        }
    }
}

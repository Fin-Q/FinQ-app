package com.swyp.FinQ.user.service;

import com.swyp.FinQ.global.exception.BaseException;
import com.swyp.FinQ.user.domain.SocialProvider;
import com.swyp.FinQ.user.domain.User;
import com.swyp.FinQ.user.exception.UserErrorCode;
import com.swyp.FinQ.user.repository.SocialAccountRepository;
import com.swyp.FinQ.user.repository.SocialAccountUnlinkTarget;
import com.swyp.FinQ.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserWithdrawalService {

    private final UserRepository userRepository;
    private final SocialAccountRepository socialAccountRepository;
    private final KakaoAccountUnlinker kakaoAccountUnlinker;
    private final AppleTokenRevoker appleTokenRevoker;
    private final OAuthTokenCipher tokenCipher;

    @Transactional
    public void withdraw(Long userId) {
        User user = userRepository.findForUpdateById(userId)
                .orElseThrow(() -> BaseException.of(UserErrorCode.USER_NOT_FOUND));

        socialAccountRepository.findAllUnlinkTargetsByUserId(userId)
                .forEach(this::unlinkSocialAccount);

        userRepository.delete(user);
        userRepository.flush();
    }

    private void unlinkSocialAccount(SocialAccountUnlinkTarget socialAccount) {
        if (socialAccount.provider() == SocialProvider.KAKAO) {
            kakaoAccountUnlinker.unlink(socialAccount.providerUserId());
            return;
        }

        String encryptedRefreshToken = socialAccount.encryptedRefreshToken();
        if (encryptedRefreshToken == null || encryptedRefreshToken.isBlank()) {
            throw BaseException.of(UserErrorCode.SOCIAL_ACCOUNT_UNLINK_FAILED);
        }
        try {
            appleTokenRevoker.revoke(tokenCipher.decrypt(encryptedRefreshToken));
        } catch (IllegalArgumentException | IllegalStateException exception) {
            throw new BaseException(UserErrorCode.SOCIAL_ACCOUNT_UNLINK_FAILED, exception);
        }
    }
}

package com.swyp.FinQ.user.service;

import com.swyp.FinQ.global.exception.BaseException;
import com.swyp.FinQ.user.domain.SocialProvider;
import com.swyp.FinQ.user.domain.User;
import com.swyp.FinQ.user.exception.UserErrorCode;
import com.swyp.FinQ.user.repository.SocialAccountRepository;
import com.swyp.FinQ.user.repository.SocialAccountUnlinkTarget;
import com.swyp.FinQ.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserWithdrawalServiceTest {

    private static final Long USER_ID = 1L;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SocialAccountRepository socialAccountRepository;

    @Mock
    private KakaoAccountUnlinker kakaoAccountUnlinker;

    @Mock
    private AppleTokenRevoker appleTokenRevoker;

    @Mock
    private OAuthTokenCipher tokenCipher;

    private UserWithdrawalService service;
    private User user;

    @BeforeEach
    void setUp() {
        service = new UserWithdrawalService(
                userRepository,
                socialAccountRepository,
                kakaoAccountUnlinker,
                appleTokenRevoker,
                tokenCipher
        );
        user = User.builder().id(USER_ID).build();
        given(userRepository.findForUpdateById(USER_ID)).willReturn(Optional.of(user));
    }

    @Test
    void 일반_회원은_외부_해제_없이_탈퇴한다() {
        given(socialAccountRepository.findAllUnlinkTargetsByUserId(USER_ID)).willReturn(List.of());

        service.withdraw(USER_ID);

        verify(userRepository).delete(user);
        verify(userRepository).flush();
        verify(kakaoAccountUnlinker, never()).unlink(org.mockito.ArgumentMatchers.any());
        verify(appleTokenRevoker, never()).revoke(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void Kakao_연결을_해제한_후_탈퇴한다() {
        SocialAccountUnlinkTarget account = kakaoAccount("kakao-user-id");
        given(socialAccountRepository.findAllUnlinkTargetsByUserId(USER_ID))
                .willReturn(List.of(account));

        service.withdraw(USER_ID);

        InOrder order = inOrder(kakaoAccountUnlinker, userRepository);
        order.verify(kakaoAccountUnlinker).unlink("kakao-user-id");
        order.verify(userRepository).delete(user);
    }

    @Test
    void Apple_Refresh_Token을_복호화하고_폐기한_후_탈퇴한다() {
        SocialAccountUnlinkTarget account = appleAccount("encrypted-refresh-token");
        given(socialAccountRepository.findAllUnlinkTargetsByUserId(USER_ID))
                .willReturn(List.of(account));
        given(tokenCipher.decrypt("encrypted-refresh-token")).willReturn("apple-refresh-token");

        service.withdraw(USER_ID);

        InOrder order = inOrder(tokenCipher, appleTokenRevoker, userRepository);
        order.verify(tokenCipher).decrypt("encrypted-refresh-token");
        order.verify(appleTokenRevoker).revoke("apple-refresh-token");
        order.verify(userRepository).delete(user);
    }

    @Test
    void 복수_소셜_계정을_모두_해제한_후_탈퇴한다() {
        SocialAccountUnlinkTarget appleAccount = appleAccount("encrypted-refresh-token");
        SocialAccountUnlinkTarget kakaoAccount = kakaoAccount("kakao-user-id");
        given(socialAccountRepository.findAllUnlinkTargetsByUserId(USER_ID))
                .willReturn(List.of(appleAccount, kakaoAccount));
        given(tokenCipher.decrypt("encrypted-refresh-token")).willReturn("apple-refresh-token");

        service.withdraw(USER_ID);

        InOrder order = inOrder(appleTokenRevoker, kakaoAccountUnlinker, userRepository);
        order.verify(appleTokenRevoker).revoke("apple-refresh-token");
        order.verify(kakaoAccountUnlinker).unlink("kakao-user-id");
        order.verify(userRepository).delete(user);
    }

    @Test
    void 외부_연결_해제에_실패하면_로컬_사용자를_삭제하지_않는다() {
        SocialAccountUnlinkTarget account = kakaoAccount("kakao-user-id");
        BaseException unlinkFailure = BaseException.of(UserErrorCode.SOCIAL_ACCOUNT_UNLINK_FAILED);
        given(socialAccountRepository.findAllUnlinkTargetsByUserId(USER_ID))
                .willReturn(List.of(account));
        willThrow(unlinkFailure).given(kakaoAccountUnlinker).unlink("kakao-user-id");

        assertThatThrownBy(() -> service.withdraw(USER_ID)).isSameAs(unlinkFailure);

        verify(userRepository, never()).delete(user);
        verify(userRepository, never()).flush();
    }

    @Test
    void Apple_Refresh_Token_암호문이_없으면_로컬_사용자를_삭제하지_않는다() {
        SocialAccountUnlinkTarget account = appleAccount(null);
        given(socialAccountRepository.findAllUnlinkTargetsByUserId(USER_ID))
                .willReturn(List.of(account));

        assertThatThrownBy(() -> service.withdraw(USER_ID))
                .isInstanceOfSatisfying(BaseException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(UserErrorCode.SOCIAL_ACCOUNT_UNLINK_FAILED));

        verify(appleTokenRevoker, never()).revoke(org.mockito.ArgumentMatchers.any());
        verify(userRepository, never()).delete(user);
    }

    private SocialAccountUnlinkTarget kakaoAccount(String providerUserId) {
        return new SocialAccountUnlinkTarget(SocialProvider.KAKAO, providerUserId, null);
    }

    private SocialAccountUnlinkTarget appleAccount(String encryptedRefreshToken) {
        return new SocialAccountUnlinkTarget(
                SocialProvider.APPLE,
                "apple-user-id",
                encryptedRefreshToken
        );
    }
}

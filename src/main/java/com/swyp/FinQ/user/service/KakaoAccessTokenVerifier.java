package com.swyp.FinQ.user.service;

public interface KakaoAccessTokenVerifier {

    KakaoUserIdentity verify(String accessToken);
}

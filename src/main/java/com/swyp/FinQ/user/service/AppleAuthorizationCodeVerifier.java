package com.swyp.FinQ.user.service;

public interface AppleAuthorizationCodeVerifier {

    void verify(String authorizationCode, String nonce, AppleUserIdentity expectedIdentity);
}

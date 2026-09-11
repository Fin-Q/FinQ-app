package com.swyp.FinQ.user.service;

public interface AppleAuthorizationCodeVerifier {

    AppleAuthorizationResult verify(
            String authorizationCode,
            String nonce,
            AppleUserIdentity expectedIdentity
    );
}

package com.swyp.FinQ.user.service;

public interface AppleIdentityTokenVerifier {

    AppleUserIdentity verify(String identityToken, String nonce);
}

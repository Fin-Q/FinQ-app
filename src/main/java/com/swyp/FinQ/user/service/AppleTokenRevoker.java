package com.swyp.FinQ.user.service;

public interface AppleTokenRevoker {

    void revoke(String refreshToken);
}

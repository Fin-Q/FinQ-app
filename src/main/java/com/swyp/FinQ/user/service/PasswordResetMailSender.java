package com.swyp.FinQ.user.service;

import java.time.Duration;

public interface PasswordResetMailSender {

    void sendVerificationCode(String recipient, String verificationCode, Duration expiration);
}

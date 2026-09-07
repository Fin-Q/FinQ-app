package com.swyp.FinQ.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VerificationCodeEncoder {

    private final PasswordEncoder passwordEncoder;

    public String encode(String verificationCode) {
        return passwordEncoder.encode(verificationCode);
    }

    public boolean matches(String verificationCode, String encodedCode) {
        return passwordEncoder.matches(verificationCode, encodedCode);
    }
}

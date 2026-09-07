package com.swyp.FinQ.user.dto.res;

public record PasswordResetRequestResponse(
        String verificationId,
        long expiresIn,
        long resendAvailableIn
) {
}

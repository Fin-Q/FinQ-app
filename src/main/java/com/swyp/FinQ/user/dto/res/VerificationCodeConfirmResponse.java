package com.swyp.FinQ.user.dto.res;

public record VerificationCodeConfirmResponse(String passwordResetToken, long expiresIn) {
}

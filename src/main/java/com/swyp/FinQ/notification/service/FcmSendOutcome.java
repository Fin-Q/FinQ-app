package com.swyp.FinQ.notification.service;

public record FcmSendOutcome(
        String token,
        FcmSendStatus status
) {
}

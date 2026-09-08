package com.swyp.FinQ.notification.dto.info;

public record PushNotificationSendResult(
        int requestedCount,
        int successCount,
        int failureCount,
        int invalidTokenRemovedCount
) {

    public static PushNotificationSendResult empty() {
        return new PushNotificationSendResult(0, 0, 0, 0);
    }
}

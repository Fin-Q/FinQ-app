package com.swyp.FinQ.content.domain;

public enum CompletionStatus {
    COMPLETED, INCOMPLETE;

    public static CompletionStatus of(boolean completed) {
        return completed ? COMPLETED : INCOMPLETE;
    }
}
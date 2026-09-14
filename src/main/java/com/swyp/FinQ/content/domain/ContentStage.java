package com.swyp.FinQ.content.domain;

public enum ContentStage {
    P1, P2, F, DEEP;

    public ContentStage nextQuestionStage() {
        return switch (this) {
            case P1 -> P2;
            case P2, F -> F;
            case DEEP -> DEEP;
        };
    }
}
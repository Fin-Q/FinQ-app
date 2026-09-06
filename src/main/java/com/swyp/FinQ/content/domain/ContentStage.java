package com.swyp.FinQ.content.domain;

public enum ContentStage {
    P1(2), P2(4), F(6), DEEP(1);

    public static final int SUMMARY_BLOCK_ORDER = 5;

    private final int blockOrder;

    ContentStage(int blockOrder) {
        this.blockOrder = blockOrder;
    }

    public int getBlockOrder() {
        return blockOrder;
    }

    public ContentStage nextQuestionStage() {
        return switch (this) {
            case P1 -> P2;
            case P2, F -> F;
            case DEEP -> DEEP;
        };
    }
}
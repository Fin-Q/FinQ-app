package com.swyp.FinQ.content.domain;

public enum ContentStage {
    P1(2), P2(4), F(6), DEEP(1);

    private final int blockOrder;

    ContentStage(int blockOrder) {
        this.blockOrder = blockOrder;
    }

    public int getBlockOrder() {
        return blockOrder;
    }
}
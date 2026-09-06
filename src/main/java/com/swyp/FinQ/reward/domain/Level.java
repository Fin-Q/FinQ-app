package com.swyp.FinQ.reward.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Level {

    LV1(1, 0),
    LV2(2, 80),
    LV3(3, 180),
    LV4(4, 300);

    private final int value;
    private final int requiredXp;

    public static Level from(int totalXp) {
        Level[] levels = values();
        for (int i = levels.length - 1; i >= 0; i--) {
            if (totalXp >= levels[i].requiredXp) {
                return levels[i];
            }
        }
        return LV1;
    }

    public boolean isHigherThan(Level other) {
        return this.value > other.value;
    }
}

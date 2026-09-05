package com.swyp.FinQ.reward.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LevelTest {

    @Test
    @DisplayName("0~79XP는 LV1이다")
    void level1() {
        assertThat(Level.from(0)).isEqualTo(Level.LV1);
        assertThat(Level.from(79)).isEqualTo(Level.LV1);
    }

    @Test
    @DisplayName("80~179XP는 LV2이다")
    void level2() {
        assertThat(Level.from(80)).isEqualTo(Level.LV2);
        assertThat(Level.from(179)).isEqualTo(Level.LV2);
    }

    @Test
    @DisplayName("180~299XP는 LV3이다")
    void level3() {
        assertThat(Level.from(180)).isEqualTo(Level.LV3);
        assertThat(Level.from(299)).isEqualTo(Level.LV3);
    }

    @Test
    @DisplayName("300XP 이상은 LV4이다")
    void level4() {
        assertThat(Level.from(300)).isEqualTo(Level.LV4);
        assertThat(Level.from(999)).isEqualTo(Level.LV4);
    }

    @Test
    @DisplayName("LV2는 LV1보다 높다")
    void isHigherThan() {
        assertThat(Level.LV2.isHigherThan(Level.LV1)).isTrue();
        assertThat(Level.LV1.isHigherThan(Level.LV2)).isFalse();
        assertThat(Level.LV1.isHigherThan(Level.LV1)).isFalse();
    }
}
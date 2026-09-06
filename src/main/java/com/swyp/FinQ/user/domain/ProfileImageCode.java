package com.swyp.FinQ.user.domain;

import java.util.concurrent.ThreadLocalRandom;

public enum ProfileImageCode {
    PROFILE_01,
    PROFILE_02,
    PROFILE_03,
    PROFILE_04;

    public static ProfileImageCode random() {
        ProfileImageCode[] values = values();
        return values[ThreadLocalRandom.current().nextInt(values.length)];
    }
}

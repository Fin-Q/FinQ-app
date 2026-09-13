package com.swyp.FinQ.user.domain;

import java.util.concurrent.ThreadLocalRandom;

public enum ProfileImageCode {
    PROFILE_01("profile_01.png"),
    PROFILE_02("profile_02.png"),
    PROFILE_03("profile_03.png"),
    PROFILE_04("profile_04.png");

    private final String fileName;

    ProfileImageCode(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    public static ProfileImageCode random() {
        ProfileImageCode[] values = values();
        return values[ThreadLocalRandom.current().nextInt(values.length)];
    }
}

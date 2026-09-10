package com.swyp.FinQ.global.config;

import java.util.List;

public final class ApiTags {

    public static final String CONTENT = "CONTENT";
    public static final String HOME = "HOME";
    public static final String LEARNING = "LEARNING";
    public static final String NOTI = "NOTI";
    public static final String REWARD = "REWARD";
    public static final String STREAK = "STREAK";
    public static final String USER = "USER";

    public static final List<String> ALL = List.of(
            CONTENT,
            HOME,
            LEARNING,
            NOTI,
            REWARD,
            STREAK,
            USER
    );

    private ApiTags() {
    }
}

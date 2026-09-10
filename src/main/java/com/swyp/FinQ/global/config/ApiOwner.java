package com.swyp.FinQ.global.config;

public enum ApiOwner {

    UNASSIGNED("미지정"),
    MINJI("이민지"),
    YEZANEE("yezanee");

    private final String displayName;

    ApiOwner(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}

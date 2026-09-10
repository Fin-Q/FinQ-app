package com.swyp.FinQ.content.domain;

import java.util.List;

public enum QuestionType {
    OX(List.of("O", "X")),
    SINGLE_CHOICE(List.of("A", "B", "C", "D"));

    private final List<String> validOptions;

    QuestionType(List<String> validOptions) {
        this.validOptions = validOptions;
    }

    public List<String> getValidOptions() {
        return validOptions;
    }

    public boolean isValidOption(String optionId) {
        return validOptions.contains(optionId);
    }
}
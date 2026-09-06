package com.swyp.FinQ.content.domain;

import java.util.Set;

public enum QuestionType {
    OX(Set.of("O", "X")),
    SINGLE_CHOICE(Set.of("A", "B", "C", "D"));

    private final Set<String> validOptions;

    QuestionType(Set<String> validOptions) {
        this.validOptions = validOptions;
    }

    public Set<String> getValidOptions() {
        return validOptions;
    }

    public boolean isValidOption(String optionId) {
        return validOptions.contains(optionId);
    }
}
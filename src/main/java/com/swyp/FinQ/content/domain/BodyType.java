package com.swyp.FinQ.content.domain;

import java.util.Set;

public enum BodyType {

    EXPLANATION(BodyField.DESC, BodyField.ADD_DESC),
    CASE(BodyField.IMG, BodyField.DESC),
    COMPARISON(BodyField.TABLE_IMG, BodyField.IMG, BodyField.DESC);

    private final Set<String> fields;

    BodyType(String... fields) {
        this.fields = Set.of(fields);
    }

    public boolean includes(String field) {
        return fields.contains(field);
    }

    public static final class BodyField {
        public static final String DESC = "description";
        public static final String ADD_DESC = "additionalDescription";
        public static final String IMG = "imageUrl";
        public static final String TABLE_IMG = "tableImageUrl";

        private BodyField() {}
    }
}
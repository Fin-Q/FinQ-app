package com.swyp.FinQ.user.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AgreementPolicy {

    TERMS_OF_SERVICE("1.0", true),
    PRIVACY_POLICY("1.0", true);

    private final String version;
    private final boolean required;
}

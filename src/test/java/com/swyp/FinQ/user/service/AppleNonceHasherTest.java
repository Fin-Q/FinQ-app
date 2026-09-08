package com.swyp.FinQ.user.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AppleNonceHasherTest {

    @Test
    void raw_nonce를_SHA256_lowercase_hex_64자로_변환한다() {
        AppleNonceHasher hasher = new AppleNonceHasher();

        String hashedNonce = hasher.hash("0123456789abcdef0123456789abcdef");

        assertThat(hashedNonce)
                .hasSize(64)
                .matches("[0-9a-f]{64}")
                .isEqualTo("3eb1bd439947eb762998e566ccc2e099c791118b2f40579cc4f7da2b5061b7f9");
    }
}

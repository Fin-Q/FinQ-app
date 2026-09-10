package com.swyp.FinQ.global.exception;

import com.swyp.FinQ.notification.exception.NotificationErrorCode;
import com.swyp.FinQ.streak.exception.StreakErrorCode;
import com.swyp.FinQ.user.exception.AuthErrorCode;
import com.swyp.FinQ.user.exception.UserErrorCode;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ErrorCodeCatalogTest {

    @Test
    void 대상_도메인의_모든_에러_코드를_제공한다() {
        List<ErrorCode> expected = List.of(
                        GlobalErrorCode.values(),
                        AuthErrorCode.values(),
                        UserErrorCode.values(),
                        NotificationErrorCode.values(),
                        StreakErrorCode.values()
                ).stream()
                .flatMap(Arrays::stream)
                .map(ErrorCode.class::cast)
                .toList();

        assertThat(ErrorCodeCatalog.values())
                .containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    void 문자열_에러_코드로_실제_에러를_조회한다() {
        assertThat(ErrorCodeCatalog.require("AUTH_INVALID_REFRESH_TOKEN"))
                .isSameAs(AuthErrorCode.INVALID_REFRESH_TOKEN);
    }

    @Test
    void 등록되지_않은_에러_코드를_거부한다() {
        assertThatThrownBy(() -> ErrorCodeCatalog.require("AUTH-001"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("AUTH-001");
    }
}

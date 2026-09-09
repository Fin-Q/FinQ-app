package com.swyp.FinQ.user.dto.res;

import java.time.OffsetDateTime;

public record NicknameUpdateResponse(String nickname, OffsetDateTime updatedAt) {
}

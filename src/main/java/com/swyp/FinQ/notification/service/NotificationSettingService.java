package com.swyp.FinQ.notification.service;

import com.swyp.FinQ.global.exception.BaseException;
import com.swyp.FinQ.notification.dto.req.NotificationSettingUpdateRequest;
import com.swyp.FinQ.notification.dto.res.NotificationSettingResponse;
import com.swyp.FinQ.user.domain.User;
import com.swyp.FinQ.user.exception.UserErrorCode;
import com.swyp.FinQ.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationSettingService {

    private final UserRepository userRepository;

    @Transactional
    public NotificationSettingResponse update(
            Long userId,
            NotificationSettingUpdateRequest request
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> BaseException.of(UserErrorCode.USER_NOT_FOUND));
        user.updateNotificationEnabled(request.notificationEnabled());

        return NotificationSettingResponse.from(userRepository.saveAndFlush(user));
    }
}

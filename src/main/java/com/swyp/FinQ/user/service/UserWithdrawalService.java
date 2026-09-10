package com.swyp.FinQ.user.service;

import com.swyp.FinQ.global.exception.BaseException;
import com.swyp.FinQ.user.domain.User;
import com.swyp.FinQ.user.exception.UserErrorCode;
import com.swyp.FinQ.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserWithdrawalService {

    private final UserRepository userRepository;

    @Transactional
    public void withdraw(Long userId) {
        User user = userRepository.findForUpdateById(userId)
                .orElseThrow(() -> BaseException.of(UserErrorCode.USER_NOT_FOUND));

        userRepository.delete(user);
        userRepository.flush();
    }
}

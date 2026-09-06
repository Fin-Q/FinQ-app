package com.swyp.FinQ.user.service;

import com.swyp.FinQ.global.exception.BaseException;
import com.swyp.FinQ.reward.service.XpQueryService;
import com.swyp.FinQ.user.domain.User;
import com.swyp.FinQ.user.dto.req.ProfileUpdateRequest;
import com.swyp.FinQ.user.dto.res.MyPageResponse;
import com.swyp.FinQ.user.exception.UserErrorCode;
import com.swyp.FinQ.user.repository.UserInterestRepository;
import com.swyp.FinQ.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserRepository userRepository;
    private final UserInterestRepository userInterestRepository;
    private final XpQueryService xpQueryService;

    @Transactional(readOnly = true)
    public MyPageResponse getMyPage(Long userId) {
        User user = getUser(userId);
        return toResponse(user);
    }

    @Transactional
    public MyPageResponse updateProfile(Long userId, ProfileUpdateRequest request) {
        if (request.nickname() == null && request.profileImageCode() == null) {
            throw BaseException.of(UserErrorCode.PROFILE_UPDATE_EMPTY);
        }

        User user = getUser(userId);
        user.updateProfile(
                request.nickname() == null ? null : request.nickname().trim(),
                request.profileImageCode()
        );
        return toResponse(user);
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> BaseException.of(UserErrorCode.USER_NOT_FOUND));
    }

    private MyPageResponse toResponse(User user) {
        return MyPageResponse.of(
                user,
                xpQueryService.getTotalXp(user.getId()),
                userInterestRepository.findAllWithCategoryByUserId(user.getId())
        );
    }
}

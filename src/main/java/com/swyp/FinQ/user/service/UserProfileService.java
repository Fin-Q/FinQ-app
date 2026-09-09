package com.swyp.FinQ.user.service;

import com.swyp.FinQ.global.exception.BaseException;
import com.swyp.FinQ.streak.service.StreakQueryService;
import com.swyp.FinQ.user.domain.User;
import com.swyp.FinQ.user.dto.req.NicknameUpdateRequest;
import com.swyp.FinQ.user.dto.req.ProfileImageUpdateRequest;
import com.swyp.FinQ.user.dto.res.NicknameUpdateResponse;
import com.swyp.FinQ.user.dto.res.ProfileImageUpdateResponse;
import com.swyp.FinQ.user.dto.res.MyPageResponse;
import com.swyp.FinQ.user.exception.UserErrorCode;
import com.swyp.FinQ.user.repository.UserInterestRepository;
import com.swyp.FinQ.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserRepository userRepository;
    private final UserInterestRepository userInterestRepository;
    private final StreakQueryService streakQueryService;

    @Transactional(readOnly = true)
    public MyPageResponse getMyPage(Long userId) {
        User user = getUser(userId);
        return toResponse(user);
    }

    @Transactional
    public NicknameUpdateResponse updateNickname(Long userId, NicknameUpdateRequest request) {
        User user = getUser(userId);
        user.updateProfile(request.nickname().trim(), null);
        userRepository.saveAndFlush(user);
        return new NicknameUpdateResponse(
                user.getNickname(),
                user.getUpdatedAt().atZone(ZoneId.systemDefault())
                        .withZoneSameInstant(ZoneId.of("Asia/Seoul")).toOffsetDateTime()
        );
    }

    @Transactional
    public ProfileImageUpdateResponse updateProfileImage(Long userId, ProfileImageUpdateRequest request) {
        User user = getUser(userId);
        user.updateProfile(null, request.profileImageCode());
        return new ProfileImageUpdateResponse(user.getProfileImageCode());
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> BaseException.of(UserErrorCode.USER_NOT_FOUND));
    }

    private MyPageResponse toResponse(User user) {
        return MyPageResponse.of(
                user,
                user.getTotalXp(),
                streakQueryService.getCurrentStreak(user.getId()),
                userInterestRepository.findAllWithCategoryByUserId(user.getId())
        );
    }
}

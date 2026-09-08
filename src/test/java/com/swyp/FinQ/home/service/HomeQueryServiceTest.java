package com.swyp.FinQ.home.service;

import com.swyp.FinQ.content.repository.ContentRepository;
import com.swyp.FinQ.home.dto.res.HomeResponse;
import com.swyp.FinQ.learning.repository.UserContentCompletionRepository;
import com.swyp.FinQ.streak.service.StreakQueryService;
import com.swyp.FinQ.user.domain.User;
import com.swyp.FinQ.user.repository.UserInterestRepository;
import com.swyp.FinQ.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class HomeQueryServiceTest {

    @InjectMocks
    private HomeQueryService homeQueryService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserInterestRepository userInterestRepository;

    @Mock
    private ContentRepository contentRepository;

    @Mock
    private UserContentCompletionRepository userContentCompletionRepository;

    @Mock
    private StreakQueryService streakQueryService;

    @Test
    void usesLogBasedCurrentStreakInsteadOfStoredValue() {
        User user = User.builder()
                .id(1L)
                .nickname("Minter")
                .totalXp(0)
                .currentStreak(7)
                .build();
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(userInterestRepository.findAllWithCategoryByUserId(1L)).willReturn(java.util.List.of());
        given(streakQueryService.getCurrentStreak(1L)).willReturn(0);

        HomeResponse response = homeQueryService.getHome(1L);

        assertThat(response.currentStreak()).isZero();
    }
}

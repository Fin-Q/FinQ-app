package com.swyp.FinQ.user.service;

import com.swyp.FinQ.global.exception.BaseException;
import com.swyp.FinQ.global.security.token.IssuedTokenPair;
import com.swyp.FinQ.user.domain.User;
import com.swyp.FinQ.user.dto.req.LoginRequest;
import com.swyp.FinQ.user.dto.res.LoginResponse;
import com.swyp.FinQ.user.exception.AuthErrorCode;
import com.swyp.FinQ.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class LoginService {

    private static final ZoneId SERVICE_ZONE_ID = ZoneId.of("Asia/Seoul");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthTokenService authTokenService;
    private final Clock clock;

    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> BaseException.of(AuthErrorCode.INVALID_CREDENTIALS));

        if (user.getPassword() == null || !passwordEncoder.matches(request.password(), user.getPassword())) {
            throw BaseException.of(AuthErrorCode.INVALID_CREDENTIALS);
        }

        user.updateLastLoginAt(LocalDateTime.ofInstant(clock.instant(), SERVICE_ZONE_ID));
        IssuedTokenPair tokens = authTokenService.issue(user);

        return new LoginResponse(
                String.valueOf(user.getId()),
                user.getNickname(),
                tokens.accessToken(),
                tokens.refreshToken(),
                "Bearer",
                tokens.accessTokenExpiresIn(),
                user.getOnboardingStatus()
        );
    }
}

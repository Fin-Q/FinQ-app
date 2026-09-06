package com.swyp.FinQ.user.domain;

import com.swyp.FinQ.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "email", unique = true, length = 255)
    private String email;

    @Column(name = "password", length = 255)
    private String password;

    @Column(name = "nickname", nullable = false, length = 50)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(name = "profile_image_code", nullable = false, length = 50)
    private ProfileImageCode profileImageCode;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "onboarding_status", nullable = false, length = 30)
    private OnboardingStatus onboardingStatus = OnboardingStatus.INTEREST_SELECTION;

    @Column(name = "onboarding_completed_at")
    private LocalDateTime onboardingCompletedAt;

    @Builder.Default
    @Column(name = "notification_enabled", nullable = false)
    private boolean notificationEnabled = true;

    @Builder.Default
    @Column(name = "total_xp", nullable = false)
    private int totalXp = 0;

    @Builder.Default
    @Column(name = "current_streak", nullable = false)
    private int currentStreak = 0;

    @Column(name = "last_streak_date")
    private LocalDate lastStreakDate;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;
}

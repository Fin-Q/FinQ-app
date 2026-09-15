package com.swyp.FinQ.streak.repository;

public interface StreakStatisticsCounts {
    long getLearnedUserCount();
    long getStreak3DaysUserCount();
    long getStreak7DaysUserCount();
}

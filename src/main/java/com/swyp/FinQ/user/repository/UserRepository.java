package com.swyp.FinQ.user.repository;

import com.swyp.FinQ.user.domain.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from User u where u.id = :userId")
    Optional<User> findForUpdateById(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE User u SET u.homeQuestionDate = :date, u.homeQuestionIds = :ids WHERE u.id = :userId")
    void updateHomeQuestionCache(@Param("userId") Long userId, @Param("date") LocalDate date, @Param("ids") String ids);
}

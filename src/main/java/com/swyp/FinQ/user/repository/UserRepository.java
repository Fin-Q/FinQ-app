package com.swyp.FinQ.user.repository;

import com.swyp.FinQ.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Modifying
    @Query("UPDATE User u SET u.homeQuestionDate = :date, u.homeQuestionIds = :ids WHERE u.id = :userId")
    void updateHomeQuestionCache(@Param("userId") Long userId, @Param("date") LocalDate date, @Param("ids") String ids);
}

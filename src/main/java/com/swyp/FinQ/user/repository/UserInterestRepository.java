package com.swyp.FinQ.user.repository;

import com.swyp.FinQ.user.domain.UserInterest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserInterestRepository extends JpaRepository<UserInterest, Long> {

    List<UserInterest> findAllByUserId(Long userId);

    @Query("""
            SELECT interest
            FROM UserInterest interest
            JOIN FETCH interest.category category
            WHERE interest.user.id = :userId
            ORDER BY category.displayOrder
            """)
    List<UserInterest> findAllWithCategoryByUserId(@Param("userId") Long userId);

    boolean existsByUserId(Long userId);

    void deleteAllByUserId(Long userId);
}

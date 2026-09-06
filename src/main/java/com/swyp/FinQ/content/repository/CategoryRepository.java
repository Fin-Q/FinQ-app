package com.swyp.FinQ.content.repository;

import com.swyp.FinQ.content.domain.Category;
import com.swyp.FinQ.content.domain.CategoryCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByCategoryCode(CategoryCode categoryCode);

    List<Category> findAllByOrderByDisplayOrder();

    List<Category> findAllByCategoryCodeIn(Set<CategoryCode> categoryCodes);
}

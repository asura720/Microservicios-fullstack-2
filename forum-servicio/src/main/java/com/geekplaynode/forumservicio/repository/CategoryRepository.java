package com.geekplaynode.forumservicio.repository;

import com.geekplaynode.forumservicio.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    Optional<Category> findBySlug(String slug);
    
    boolean existsBySlug(String slug);
}
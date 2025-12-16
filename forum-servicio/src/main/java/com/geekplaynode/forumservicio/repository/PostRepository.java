package com.geekplaynode.forumservicio.repository;

import com.geekplaynode.forumservicio.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    
    // Buscar posts por categoría
    List<Post> findByCategoryId(Long categoryId);
    
    // Buscar posts por slug de categoría
    List<Post> findByCategorySlug(String slug);
    
    // Buscar posts por autor
    List<Post> findByAutorId(Long autorId);
    
    // Ordenar posts por fecha (más recientes primero)
    List<Post> findAllByOrderByCreadoEnDesc();
    
    List<Post> findByCategoryIdOrderByCreadoEnDesc(Long categoryId);
}
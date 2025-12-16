package com.geekplaynode.forumservicio.repository;

import com.geekplaynode.forumservicio.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    
    // Buscar comentarios por post
    List<Comment> findByPostId(Long postId);
    
    // Buscar comentarios por post ordenados por fecha (más recientes primero)
    List<Comment> findByPostIdOrderByCreadoEnDesc(Long postId);
    
    // Buscar comentarios por autor
    List<Comment> findByAutorId(Long autorId);
    
    // Contar comentarios de un post
    long countByPostId(Long postId);
}
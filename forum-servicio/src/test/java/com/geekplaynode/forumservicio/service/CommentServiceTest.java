package com.geekplaynode.forumservicio.service;

import com.geekplaynode.forumservicio.dto.CommentResponse;
import com.geekplaynode.forumservicio.dto.CreateCommentRequest;
import com.geekplaynode.forumservicio.model.Comment;
import com.geekplaynode.forumservicio.model.Post;
import com.geekplaynode.forumservicio.repository.CommentRepository;
import com.geekplaynode.forumservicio.repository.PostRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock private CommentRepository commentRepository;
    @Mock private PostRepository postRepository;

    @InjectMocks
    private CommentService commentService;

    @Test
    void createComment_DeberiaFuncionar_SiPostExiste() {
        // 1. Arrange
        Long postId = 1L;
        CreateCommentRequest request = new CreateCommentRequest();
        request.setContenido("Buen post!");

        Post post = Post.builder().id(postId).titulo("Post").build();
        
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        
        Comment savedComment = Comment.builder()
                .id(10L)
                .contenido("Buen post!")
                .autorNombre("Usuario")
                .post(post)
                .build();
        when(commentRepository.save(any(Comment.class))).thenReturn(savedComment);

        // 2. Act
        CommentResponse response = commentService.createComment(postId, request, 5L, "Usuario", "avatar.png");

        // 3. Assert
        assertNotNull(response);
        assertEquals("Buen post!", response.getContenido());
    }

    @Test
    void deleteComment_DeberiaFallar_SiUsuarioNoEsAutorNiAdmin() {
        // 1. Arrange
        Long commentId = 1L;
        Long userIdIntruso = 99L;
        
        Comment comment = Comment.builder()
                .id(commentId)
                .autorId(5L) // El autor es ID 5
                .build();

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        // 2. Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            commentService.deleteComment(commentId, userIdIntruso, "USER");
        });

        assertEquals("No tienes permiso para eliminar este comentario", ex.getMessage());
        verify(commentRepository, never()).deleteById(any());
    }

    @Test
    void deleteComment_DeberiaFuncionar_SiEsAdmin() {
        // 1. Arrange
        Long commentId = 1L;
        Long adminId = 777L;
        
        Comment comment = Comment.builder()
                .id(commentId)
                .autorId(5L) // El autor es otro
                .build();

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        // 2. Act
        commentService.deleteComment(commentId, adminId, "ADMIN");

        // 3. Assert
        verify(commentRepository).deleteById(commentId);
    }
}
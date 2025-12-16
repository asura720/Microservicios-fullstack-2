package com.geekplaynode.forumservicio.service;

import com.geekplaynode.forumservicio.client.ProfileServiceClient;
import com.geekplaynode.forumservicio.dto.CreatePostRequest;
import com.geekplaynode.forumservicio.dto.PostResponse;
import com.geekplaynode.forumservicio.model.Category;
import com.geekplaynode.forumservicio.model.Post;
import com.geekplaynode.forumservicio.repository.CategoryRepository;
import com.geekplaynode.forumservicio.repository.CommentRepository;
import com.geekplaynode.forumservicio.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock private PostRepository postRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private CommentRepository commentRepository;
    @Mock private ProfileServiceClient profileServiceClient;

    @InjectMocks
    private PostService postService;

    @BeforeEach
    void setUp() {
        // Inyectamos la URL de notificación para evitar nulos, aunque no probaremos la llamada HTTP real aquí
        ReflectionTestUtils.setField(postService, "notificationServiceUrl", "http://fake-url");
    }

    @Test
    void createPost_DeberiaCrearPost_CuandoCategoriaExiste() {
        // 1. Arrange
        Long autorId = 10L;
        CreatePostRequest request = new CreatePostRequest();
        request.setCategoryId(1L);
        request.setTitulo("Mi Post");
        request.setContenido("Contenido");

        Category category = Category.builder().id(1L).nombre("General").build();
        
        // Simulamos que al guardar, devuelve un Post con ID
        Post savedPost = Post.builder()
                .id(50L)
                .titulo("Mi Post")
                .category(category)
                .autorId(autorId)
                .autorNombre("Juan")
                .build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(postRepository.save(any(Post.class))).thenReturn(savedPost);

        // 2. Act
        PostResponse response = postService.createPost(request, autorId, "Juan", null);

        // 3. Assert
        assertNotNull(response);
        assertEquals("Mi Post", response.getTitulo());
        assertEquals("Juan", response.getAutorNombre());
        verify(postRepository).save(any(Post.class));
    }

    @Test
    void deletePost_DeberiaFallar_SiUsuarioNoEsDueñoNiAdmin() {
        // 1. Arrange
        Long postId = 1L;
        Long userIdIntruso = 99L; // Usuario diferente al dueño
        
        Post post = Post.builder()
                .id(postId)
                .autorId(5L) // El dueño es el ID 5
                .titulo("Post Ajeno")
                .build();

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        // 2. Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            postService.deletePost(postId, userIdIntruso, "USER", "Motivo X");
        });

        assertEquals("No tienes permiso para eliminar este post", exception.getMessage());
        verify(postRepository, never()).deleteById(any());
    }

    @Test
    void deletePost_DeberiaFuncionar_SiEsAdmin_AunqueNoSeaDueño() {
        // 1. Arrange
        Long postId = 1L;
        Long adminId = 999L; 
        
        Post post = Post.builder()
                .id(postId)
                .autorId(5L) // El dueño es otro
                .titulo("Post Ofensivo")
                .build();

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        // 2. Act
        // Pasamos role "ADMIN"
        postService.deletePost(postId, adminId, "ADMIN", "Inapropiado");

        // 3. Assert
        verify(postRepository).deleteById(postId);
        // También verifica que se intentó borrar comentarios
        verify(commentRepository).deleteAll(any());
    }
}
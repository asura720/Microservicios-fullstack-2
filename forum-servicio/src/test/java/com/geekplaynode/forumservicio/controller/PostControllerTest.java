package com.geekplaynode.forumservicio.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.geekplaynode.forumservicio.client.ProfileServiceClient;
import com.geekplaynode.forumservicio.dto.CreatePostRequest;
import com.geekplaynode.forumservicio.dto.PostResponse;
import com.geekplaynode.forumservicio.service.PostService;
import com.geekplaynode.forumservicio.util.JwtUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication; // Importar
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PostController.class)
@AutoConfigureMockMvc(addFilters = false)
class PostControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private PostService postService;
    @MockBean private JwtUtils jwtUtils;
    @MockBean private ProfileServiceClient profileServiceClient;

    @Test
    void getAllPosts_DeberiaSerPublicoYRetornar200() throws Exception {
        // Arrange
        when(postService.getAllPosts()).thenReturn(List.of(new PostResponse()));

        // Act & Assert
        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isOk());
    }

    @Test
    void createPost_DeberiaFuncionar_ConTokenValido() throws Exception {
        // 1. Arrange
        CreatePostRequest request = CreatePostRequest.builder()
                .titulo("Hola Mundo")
                .contenido("Contenido de prueba")
                .categoryId(1L)
                .build();

        String token = "Bearer token_valido";
        Long userId = 10L;

        // --- SOLUCIÓN: Mockear Authentication ---
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("test@geekplay.com");
        // ----------------------------------------

        when(jwtUtils.extractUserId(anyString())).thenReturn(userId);
        
        // Mock del perfil
        ProfileServiceClient.ProfileData mockProfile = mock(ProfileServiceClient.ProfileData.class);
        when(mockProfile.getNombre()).thenReturn("UsuarioTest");
        when(mockProfile.getAvatar()).thenReturn("avatar.png");

        when(profileServiceClient.getProfile(userId)).thenReturn(mockProfile);

        when(postService.createPost(any(CreatePostRequest.class), eq(userId), any(), any()))
                .thenReturn(new PostResponse());

        // 2. Act & Assert
        mockMvc.perform(post("/api/posts")
                        .principal(auth) // <--- AQUÍ SE PASA EL MOCK DE AUTH
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void createPost_DeberiaRetornar401_SinToken() throws Exception {
        // Arrange
        CreatePostRequest request = CreatePostRequest.builder()
                .titulo("Intento Hacker")
                .contenido("Sin token")
                .categoryId(1L)
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deletePost_DeberiaLlamarAlServicio_ConDatosCorrectos() throws Exception {
        // Arrange
        Long postId = 50L;
        String token = "Bearer admin_token";
        
        when(jwtUtils.extractUserId(anyString())).thenReturn(1L);
        when(jwtUtils.extractRole(anyString())).thenReturn("ADMIN");

        // Act & Assert
        mockMvc.perform(delete("/api/posts/{id}", postId)
                        .header("Authorization", token)
                        .param("reason", "Spam"))
                .andExpect(status().isNoContent());
    }
}
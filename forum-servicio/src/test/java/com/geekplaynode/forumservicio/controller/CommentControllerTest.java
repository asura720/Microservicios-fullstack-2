package com.geekplaynode.forumservicio.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.geekplaynode.forumservicio.client.ProfileServiceClient;
import com.geekplaynode.forumservicio.dto.CommentResponse;
import com.geekplaynode.forumservicio.dto.CreateCommentRequest;
import com.geekplaynode.forumservicio.service.CommentService;
import com.geekplaynode.forumservicio.util.JwtUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication; // Importante
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CommentController.class)
@AutoConfigureMockMvc(addFilters = false)
class CommentControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private CommentService commentService;
    @MockBean private JwtUtils jwtUtils;
    @MockBean private ProfileServiceClient profileServiceClient;

    @Test
    void getCommentsByPost_DeberiaRetornar200_EsPublico() throws Exception {
        // Arrange
        Long postId = 1L;
        when(commentService.getCommentsByPost(postId)).thenReturn(List.of(new CommentResponse()));

        // Act & Assert
        mockMvc.perform(get("/api/comments/post/{postId}", postId))
                .andExpect(status().isOk());
    }

    @Test
    void createComment_DeberiaFuncionar_ConTokenValido() throws Exception {
        // 1. Arrange
        Long postId = 1L;
        CreateCommentRequest request = CreateCommentRequest.builder()
                .contenido("Excelente aporte")
                .build();
        
        String token = "Bearer token_usuario";
        Long userId = 10L;

        // --- SOLUCIÓN: Mockear Authentication para evitar NullPointerException ---
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("test@geekplay.com");
        // -----------------------------------------------------------------------

        when(jwtUtils.extractUserId(anyString())).thenReturn(userId);
        
        // Mock del DTO ProfileData
        ProfileServiceClient.ProfileData mockProfile = mock(ProfileServiceClient.ProfileData.class);
        when(mockProfile.getNombre()).thenReturn("UsuarioTest");
        when(mockProfile.getAvatar()).thenReturn("avatar.png");
        
        when(profileServiceClient.getProfile(userId)).thenReturn(mockProfile);
        
        when(commentService.createComment(eq(postId), any(CreateCommentRequest.class), eq(userId), any(), any()))
                .thenReturn(new CommentResponse());

        // 2. Act & Assert
        mockMvc.perform(post("/api/comments/post/{postId}", postId)
                        .principal(auth) // <--- INYECTAMOS EL MOCK AQUÍ
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void createComment_DeberiaRetornar401_SinToken() throws Exception {
         // Arrange
         CreateCommentRequest request = CreateCommentRequest.builder()
                 .contenido("Spam")
                 .build();
         
         // Act & Assert
         mockMvc.perform(post("/api/comments/post/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deleteComment_DeberiaLlamarServicio_ConDatosCorrectos() throws Exception {
        // Arrange
        Long commentId = 5L;
        String token = "Bearer admin_token";
        
        when(jwtUtils.extractUserId(anyString())).thenReturn(1L);
        when(jwtUtils.extractRole(anyString())).thenReturn("ADMIN");

        // Act & Assert
        mockMvc.perform(delete("/api/comments/{id}", commentId)
                        .header("Authorization", token))
                .andExpect(status().isNoContent());

        verify(commentService).deleteComment(eq(commentId), anyLong(), eq("ADMIN"));
    }
}
package com.geekplaynode.notificacionservicio.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.geekplaynode.notificacionservicio.dto.NotificationRequest;
import com.geekplaynode.notificacionservicio.dto.NotificationResponse;
import com.geekplaynode.notificacionservicio.service.NotificationService;
import com.geekplaynode.notificacionservicio.util.JwtUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationController.class)
@AutoConfigureMockMvc(addFilters = false)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private JwtUtils jwtUtils;

    @Test
    void createNotification_DeberiaFuncionar_EsPublico() throws Exception {
        // 1. Arrange
        NotificationRequest request = new NotificationRequest();
        request.setUserId(1L);
        request.setTitulo("Test");
        request.setMensaje("Mensaje test");
        request.setTipo("INFO");

        NotificationResponse response = new NotificationResponse(
                1L, 1L, "INFO", "Test", "Mensaje test", false, LocalDateTime.now()
        );

        when(notificationService.createNotification(any(NotificationRequest.class))).thenReturn(response);

        // 2. Act & Assert
        mockMvc.perform(post("/api/notifications/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.titulo").value("Test"));
    }

    @Test
    void getMyNotifications_DeberiaRetornarLista_ConTokenValido() throws Exception {
        // 1. Arrange
        String token = "Bearer token_valido";
        Long userId = 10L;

        when(jwtUtils.extractUserId(anyString())).thenReturn(userId);
        
        NotificationResponse response = new NotificationResponse(
                1L, userId, "INFO", "Titulo", "Msg", false, LocalDateTime.now()
        );
        when(notificationService.getMyNotifications(userId)).thenReturn(List.of(response));

        // 2. Act & Assert
        mockMvc.perform(get("/api/notifications/me")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(userId));
    }

    @Test
    void getUnreadCount_DeberiaRetornarMapa() throws Exception {
        // 1. Arrange
        String token = "Bearer token_valido";
        Long userId = 10L;

        when(jwtUtils.extractUserId(anyString())).thenReturn(userId);
        when(notificationService.countUnread(userId)).thenReturn(5L);

        // 2. Act & Assert
        mockMvc.perform(get("/api/notifications/me/unread-count")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unreadCount").value(5));
    }

    @Test
    void markAsRead_DeberiaFuncionar() throws Exception {
        // 1. Arrange
        Long notifId = 1L;
        String token = "Bearer token_valido";
        Long userId = 10L;

        when(jwtUtils.extractUserId(anyString())).thenReturn(userId);

        NotificationResponse response = new NotificationResponse(
                notifId, userId, "INFO", "Titulo", "Msg", true, LocalDateTime.now()
        );
        when(notificationService.markAsRead(notifId, userId)).thenReturn(response);

        // 2. Act & Assert
        mockMvc.perform(put("/api/notifications/{id}/read", notifId)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.leida").value(true));
    }

    @Test
    void deleteNotification_DeberiaRetornarMensaje() throws Exception {
        // 1. Arrange
        Long notifId = 1L;
        String token = "Bearer token_valido";
        Long userId = 10L;

        when(jwtUtils.extractUserId(anyString())).thenReturn(userId);

        // 2. Act & Assert
        mockMvc.perform(delete("/api/notifications/{id}", notifId)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Notificación eliminada correctamente"));
        
        verify(notificationService).deleteNotification(notifId, userId);
    }
}
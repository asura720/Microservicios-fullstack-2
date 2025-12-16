package com.geekplaynode.autenticacionservicio.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.geekplaynode.autenticacionservicio.dto.BanUserRequest;
import com.geekplaynode.autenticacionservicio.dto.UserResponse;
import com.geekplaynode.autenticacionservicio.service.AdminService;
import com.geekplaynode.autenticacionservicio.util.JwtUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private AdminService adminService;
    @MockBean private JwtUtils jwtUtils; // Importante mockear esto porque el controlador lo usa directo

    @Test
    void getAllUsers_DeberiaRetornar200_SiEsAdmin() throws Exception {
        // 1. Arrange
        String token = "Bearer token_admin_valido";
        when(jwtUtils.extractRole(anyString())).thenReturn("ADMIN"); // Simulamos que el token dice que es ADMIN
        when(adminService.getAllUsers()).thenReturn(List.of(new UserResponse()));

        // 2. Act & Assert
        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", token)) // Enviamos el header manualmente
                .andExpect(status().isOk());
    }

    @Test
    void getAllUsers_DeberiaRetornar403_SiEsUser() throws Exception {
        // 1. Arrange
        String token = "Bearer token_usuario_normal";
        when(jwtUtils.extractRole(anyString())).thenReturn("USER"); // Simulamos que es un mortal

        // 2. Act & Assert
        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", token))
                .andExpect(status().isForbidden()); // Esperamos el 403
        
        verify(adminService, never()).getAllUsers(); // Aseguramos que nunca se llamara al servicio
    }

    @Test
    void getAllUsers_DeberiaRetornar401_SiNoHayToken() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/admin/users")) // Sin header
                .andExpect(status().isUnauthorized()); // Esperamos el 401
    }

    @Test
    void banUser_DeberiaFuncionar_SiEsAdmin() throws Exception {
        // 1. Arrange
        Long userId = 99L;
        String token = "Bearer token_admin";
        BanUserRequest request = new BanUserRequest(); // Asumo que tienes este DTO
        request.setRazon("Comportamiento indebido");

        when(jwtUtils.extractRole(anyString())).thenReturn("ADMIN");

        // 2. Act & Assert
        mockMvc.perform(post("/api/admin/users/{userId}/ban", userId)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Verificamos que el servicio se llamó con los datos correctos
        verify(adminService).banUser(eq(userId), eq("Comportamiento indebido"));
    }
}
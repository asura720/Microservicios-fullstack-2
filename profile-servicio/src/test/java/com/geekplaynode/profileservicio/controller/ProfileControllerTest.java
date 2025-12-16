package com.geekplaynode.profileservicio.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.geekplaynode.profileservicio.dto.CreateProfileRequest;
import com.geekplaynode.profileservicio.dto.ProfileResponse;
import com.geekplaynode.profileservicio.dto.UpdateProfileRequest;
import com.geekplaynode.profileservicio.model.Role;
import com.geekplaynode.profileservicio.service.ProfileService;
import com.geekplaynode.profileservicio.util.JwtUtils; // Importante para el contexto de seguridad
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows; // Importante
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProfileController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProfileControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private ProfileService profileService;
    @MockBean private JwtUtils jwtUtils; // Necesario para levantar SecurityConfig

    // --- HAPPY PATHS (Casos de Éxito) ---

    @Test
    void createProfile_DeberiaFuncionar_EsPublicoOInterno() throws Exception {
        // Arrange
        CreateProfileRequest request = CreateProfileRequest.builder()
                .userId(1L)
                .email("test@mail.com")
                .nombre("Test")
                .role(Role.USER)
                .build();

        // Mockeamos la respuesta del servicio
        ProfileResponse mockResponse = mock(ProfileResponse.class);
        when(profileService.createProfile(any(CreateProfileRequest.class))).thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(post("/api/profile/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void getMyProfile_DeberiaRetornarPerfil_ConToken() throws Exception {
        // Arrange
        String email = "user@mail.com";
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn(email);

        ProfileResponse mockResponse = mock(ProfileResponse.class);
        when(profileService.getMyProfile(email)).thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(get("/api/profile/me")
                        .principal(auth)) // Inyectamos el usuario autenticado
                .andExpect(status().isOk());
    }

    @Test
    void updateMyProfile_DeberiaFuncionar() throws Exception {
        // Arrange
        String email = "user@mail.com";
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn(email);

        UpdateProfileRequest request = new UpdateProfileRequest(); 
        // Si UpdateProfileRequest tiene @Builder, úsalo. Si no, new() está bien.
        
        ProfileResponse mockResponse = mock(ProfileResponse.class);
        when(profileService.updateMyProfile(eq(email), any(UpdateProfileRequest.class)))
                .thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(put("/api/profile/me")
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void getProfileById_DeberiaFuncionar() throws Exception {
        // Arrange
        Long id = 5L;
        ProfileResponse mockResponse = mock(ProfileResponse.class);
        when(profileService.getProfileById(id)).thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(get("/api/profile/{id}", id))
                .andExpect(status().isOk());
    }

    // --- SAD PATHS (Casos de Error) ---

    @Test
    void createProfile_DeberiaFallar_SiEmailYaExiste() {
        // Arrange
        CreateProfileRequest request = CreateProfileRequest.builder()
                .email("repetido@mail.com")
                .build();

        when(profileService.createProfile(any(CreateProfileRequest.class)))
                .thenThrow(new RuntimeException("El perfil ya existe"));

        // Act & Assert
        // Como no hay @ControllerAdvice, la excepción sale del controller.
        // Usamos assertThrows para verificar que "explota" con la excepción correcta.
        Exception exception = assertThrows(Exception.class, () -> {
            mockMvc.perform(post("/api/profile/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)));
        });

        // Verificamos que la causa raíz sea nuestra RuntimeException
        assertTrue(exception.getCause() instanceof RuntimeException);
        assertEquals("El perfil ya existe", exception.getCause().getMessage());
    }

    @Test
    void getProfileById_DeberiaFallar_SiNoExiste() {
        // Arrange
        Long id = 99L;
        when(profileService.getProfileById(id))
                .thenThrow(new RuntimeException("Perfil no encontrado"));

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            mockMvc.perform(get("/api/profile/{id}", id));
        });

        assertTrue(exception.getCause() instanceof RuntimeException);
        assertEquals("Perfil no encontrado", exception.getCause().getMessage());
    }
}
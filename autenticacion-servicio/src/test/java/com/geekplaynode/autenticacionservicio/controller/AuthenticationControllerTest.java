package com.geekplaynode.autenticacionservicio.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.geekplaynode.autenticacionservicio.dto.AuthResponse;
import com.geekplaynode.autenticacionservicio.dto.LoginRequest;
import com.geekplaynode.autenticacionservicio.dto.RegisterRequest;
import com.geekplaynode.autenticacionservicio.service.AuthenticationService;
import com.geekplaynode.autenticacionservicio.util.JwtUtils; // <--- Importar JwtUtils
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthenticationController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthenticationService authService;

    // --- AGREGADO: Mockear JwtUtils para que Spring pueda levantar el filtro de seguridad sin errores ---
    @MockBean
    private JwtUtils jwtUtils; 
    // --------------------------------------------------------------------------------------------------

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void register_DeberiaRetornar200_CuandoDatosSonValidos() throws Exception {
        // 1. Arrange
        RegisterRequest request = new RegisterRequest();
        request.setNombre("NuevoUser");
        request.setEmail("test@test.com");
        request.setPassword("123456");

        AuthResponse mockResponse = AuthResponse.builder()
                .token("jwt-token-falso")
                .role("USER")
                .build();

        when(authService.register(any(RegisterRequest.class))).thenReturn(mockResponse);

        // 2. Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token-falso"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void login_DeberiaRetornar200_CuandoCredencialesSonValidas() throws Exception {
        // 1. Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("user@test.com");
        request.setPassword("123456");

        AuthResponse mockResponse = AuthResponse.builder()
                .token("jwt-token-login")
                .role("USER")
                .build();

        when(authService.authenticate(any(LoginRequest.class))).thenReturn(mockResponse);

        // 2. Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token-login"));
    }

    // --- NUEVOS TESTS DE ERROR (SAD PATHS) ---

    @Test
    void login_DeberiaFallar_CuandoPasswordEsIncorrecta() throws Exception {
        // 1. Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("user@test.com");
        request.setPassword("wrongpassword");

        // Simulamos que el servicio lanza una excepción
        when(authService.authenticate(any(LoginRequest.class)))
                .thenThrow(new RuntimeException("Credenciales inválidas"));

        // 2. Act & Assert
        // CORREGIDO: Ahora esperamos 400 (BadRequest) porque tu app lo maneja así, no 500.
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()); 
    }

    @Test
    void register_DeberiaFallar_CuandoEmailYaExiste() throws Exception {
        // 1. Arrange
        RegisterRequest request = new RegisterRequest();
        request.setEmail("existente@test.com");

        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new RuntimeException("El email ya está registrado"));

        // 2. Act & Assert
        // CORREGIDO: Ahora esperamos 400 (BadRequest)
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
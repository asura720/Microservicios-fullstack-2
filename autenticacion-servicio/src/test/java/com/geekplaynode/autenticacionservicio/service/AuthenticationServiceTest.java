package com.geekplaynode.autenticacionservicio.service;

import com.geekplaynode.autenticacionservicio.client.ProfileServiceClient;
import com.geekplaynode.autenticacionservicio.dto.AuthResponse;
import com.geekplaynode.autenticacionservicio.dto.LoginRequest;
import com.geekplaynode.autenticacionservicio.dto.RegisterRequest;
import com.geekplaynode.autenticacionservicio.model.Role;
import com.geekplaynode.autenticacionservicio.model.User;
import com.geekplaynode.autenticacionservicio.repository.UserRepository;
import com.geekplaynode.autenticacionservicio.util.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtils jwtUtils;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private ProfileServiceClient profileServiceClient;

    @InjectMocks
    private AuthenticationService authService;

    // Seteamos la clave secreta falsa para probar
    private final String FAKE_ADMIN_KEY = "super_secret_key";

    @BeforeEach
    void setUp() {
        // Usamos ReflectionTestUtils para inyectar el valor de @Value("${geekplay.admin.secret}")
        ReflectionTestUtils.setField(authService, "adminSecretKey", FAKE_ADMIN_KEY);
    }

    @Test
    void register_DeberiaCrearUsuarioUser_PorDefecto() {
        // 1. Arrange
        RegisterRequest request = new RegisterRequest(); // Asumiendo que tiene setters o constructor
        request.setNombre("Pepe");
        request.setEmail("pepe@gmail.com");
        request.setPassword("123456");

        User userGuardado = User.builder()
                .id(1L)
                .nombre("Pepe")
                .email("pepe@gmail.com")
                .role(Role.USER)
                .build();

        when(passwordEncoder.encode(any())).thenReturn("encoded_pass");
        when(userRepository.save(any(User.class))).thenReturn(userGuardado);
        when(jwtUtils.generateToken(any(User.class))).thenReturn("fake-jwt-token");

        // 2. Act
        AuthResponse response = authService.register(request);

        // 3. Assert
        assertNotNull(response);
        assertEquals("USER", response.getRole());
        verify(profileServiceClient).createProfile(eq(1L), eq("Pepe"), eq("pepe@gmail.com"), eq(Role.USER));
    }

    @Test
    void register_DeberiaCrearAdmin_ConEmailYKeyCorrectos() {
        // 1. Arrange
        RegisterRequest request = new RegisterRequest();
        request.setNombre("Admin");
        request.setEmail("boss@geekplay.com"); // Email de dominio correcto
        request.setPassword("123456");
        request.setAdminKey(FAKE_ADMIN_KEY); // Key correcta

        User userGuardado = User.builder()
                .id(2L)
                .nombre("Admin")
                .role(Role.ADMIN) // Esperamos que sea admin
                .build();

        when(passwordEncoder.encode(any())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(userGuardado);
        when(jwtUtils.generateToken(any(User.class))).thenReturn("token");

        // 2. Act
        AuthResponse response = authService.register(request);

        // 3. Assert
        assertEquals("ADMIN", response.getRole());
    }

    @Test
    void authenticate_DeberiaFallar_SiUsuarioEstaBaneado() {
        // 1. Arrange
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("baneado@test.com");
        loginRequest.setPassword("1234");

        User bannedUser = User.builder()
                .email("baneado@test.com")
                .baneado(true)
                .motivoBaneo("Hackeo")
                .build();

        when(userRepository.findByEmail("baneado@test.com")).thenReturn(Optional.of(bannedUser));

        // 2. Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.authenticate(loginRequest);
        });

        assertTrue(exception.getMessage().contains("Tu cuenta ha sido suspendida"));
        assertTrue(exception.getMessage().contains("Hackeo"));
        
        // Importante: Asegurar que NO se llamó al AuthenticationManager ni se generó token
        verify(authenticationManager, never()).authenticate(any());
        verify(jwtUtils, never()).generateToken(any());
    }
}
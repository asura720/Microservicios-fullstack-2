package com.geekplaynode.autenticacionservicio.service;

import com.geekplaynode.autenticacionservicio.dto.UserResponse;
import com.geekplaynode.autenticacionservicio.model.User;
import com.geekplaynode.autenticacionservicio.repository.UserRepository;
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
class AdminServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AdminService adminService;

    @Test
    void banUser_DeberiaBanearUsuario_CuandoNoEstaBaneado() {
        // 1. Arrange
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setBaneado(false);

        // Simulamos que encontramos al usuario
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        // Simulamos el guardado
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // 2. Act
        adminService.banUser(userId, "Comportamiento tóxico");

        // 3. Assert
        assertTrue(user.getBaneado()); // Verificamos que el flag cambió
        assertEquals("Comportamiento tóxico", user.getMotivoBaneo());
        verify(userRepository).save(user); // Verificamos que se llamó a guardar
    }

    @Test
    void banUser_DeberiaLanzarExcepcion_CuandoYaEstaBaneado() {
        // 1. Arrange
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setBaneado(true); // Ya está baneado

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // 2. Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            adminService.banUser(userId, "Cualquier motivo");
        });

        assertEquals("El usuario ya está baneado", exception.getMessage());
        verify(userRepository, never()).save(any(User.class)); // Aseguramos que NO se guardó nada
    }

    @Test
    void unbanUser_DeberiaDesbanear_CuandoEstaBaneado() {
        // 1. Arrange
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setBaneado(true);
        user.setMotivoBaneo("Spam");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // 2. Act
        adminService.unbanUser(userId);

        // 3. Assert
        assertFalse(user.getBaneado());
        assertNull(user.getMotivoBaneo());
        verify(userRepository).save(user);
    }
}
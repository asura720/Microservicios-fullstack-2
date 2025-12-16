package com.geekplaynode.notificacionservicio.service;

import com.geekplaynode.notificacionservicio.dto.NotificationRequest;
import com.geekplaynode.notificacionservicio.dto.NotificationResponse;
import com.geekplaynode.notificacionservicio.model.Notification;
import com.geekplaynode.notificacionservicio.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void createNotification_DeberiaGuardarYRetornarResponse() {
        // 1. Arrange
        NotificationRequest request = new NotificationRequest();
        request.setUserId(1L);
        request.setTitulo("Hola");
        request.setMensaje("Mensaje de prueba");
        request.setTipo("INFO");

        // Simulamos la entidad guardada en BD
        Notification savedNotification = new Notification();
        savedNotification.setId(10L);
        savedNotification.setUserId(1L);
        savedNotification.setTitulo("Hola");
        savedNotification.setMensaje("Mensaje de prueba");
        savedNotification.setTipo("INFO");
        savedNotification.setLeida(false);
        savedNotification.setCreadoEn(LocalDateTime.now());

        when(notificationRepository.save(any(Notification.class))).thenReturn(savedNotification);

        // 2. Act
        NotificationResponse response = notificationService.createNotification(request);

        // 3. Assert
        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals("Hola", response.getTitulo());
        assertEquals(false, response.getLeida());
        
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void getMyNotifications_DeberiaRetornarLista() {
        // 1. Arrange
        Long userId = 1L;
        Notification n1 = new Notification();
        n1.setId(1L);
        n1.setUserId(userId);
        n1.setCreadoEn(LocalDateTime.now());
        
        when(notificationRepository.findByUserIdOrderByCreadoEnDesc(userId))
                .thenReturn(List.of(n1));

        // 2. Act
        List<NotificationResponse> list = notificationService.getMyNotifications(userId);

        // 3. Assert
        assertFalse(list.isEmpty());
        assertEquals(1, list.size());
    }

    @Test
    void markAsRead_DeberiaFuncionar_SiEsPropietario() {
        // 1. Arrange
        Long notifId = 1L;
        Long userId = 5L;

        Notification existing = new Notification();
        existing.setId(notifId);
        existing.setUserId(userId); // El usuario coincide
        existing.setLeida(false);
        existing.setCreadoEn(LocalDateTime.now());

        when(notificationRepository.findById(notifId)).thenReturn(Optional.of(existing));
        // Simulamos que al guardar retorna el mismo objeto actualizado
        when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> i.getArguments()[0]);

        // 2. Act
        NotificationResponse response = notificationService.markAsRead(notifId, userId);

        // 3. Assert
        assertTrue(response.getLeida());
        verify(notificationRepository).save(existing);
    }

    @Test
    void markAsRead_DeberiaFallar_SiEsOtroUsuario() {
        // 1. Arrange
        Long notifId = 1L;
        Long userIdIntruso = 99L;
        Long ownerId = 5L;

        Notification existing = new Notification();
        existing.setId(notifId);
        existing.setUserId(ownerId); // El dueño es el 5

        when(notificationRepository.findById(notifId)).thenReturn(Optional.of(existing));

        // 2. Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            notificationService.markAsRead(notifId, userIdIntruso);
        });

        assertEquals("No tienes permiso para marcar esta notificación", ex.getMessage());
    }

    @Test
    void deleteNotification_DeberiaBorrar_SiEsPropietario() {
        // 1. Arrange
        Long notifId = 1L;
        Long userId = 5L;

        Notification existing = new Notification();
        existing.setId(notifId);
        existing.setUserId(userId);

        when(notificationRepository.findById(notifId)).thenReturn(Optional.of(existing));

        // 2. Act
        notificationService.deleteNotification(notifId, userId);

        // 3. Assert
        verify(notificationRepository).delete(existing);
    }
}
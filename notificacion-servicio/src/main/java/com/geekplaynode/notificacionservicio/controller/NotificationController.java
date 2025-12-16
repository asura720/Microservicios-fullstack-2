package com.geekplaynode.notificacionservicio.controller;

import com.geekplaynode.notificacionservicio.dto.NotificationRequest;
import com.geekplaynode.notificacionservicio.dto.NotificationResponse;
import com.geekplaynode.notificacionservicio.service.NotificationService;
import com.geekplaynode.notificacionservicio.util.JwtUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notificaciones", description = "API para gestión de notificaciones de usuarios")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/create")
    @Operation(summary = "Crear notificación", description = "Crea una nueva notificación (endpoint público para microservicios).")
    @ApiResponse(responseCode = "200", description = "Notificación creada exitosamente")
    @ApiResponse(responseCode = "400", description = "Datos inválidos")
    public ResponseEntity<NotificationResponse> createNotification(@RequestBody NotificationRequest request) {
        NotificationResponse response = notificationService.createNotification(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @Operation(summary = "Obtener mis notificaciones", description = "Obtiene todas las notificaciones del usuario autenticado.")
    @ApiResponse(responseCode = "200", description = "Notificaciones obtenidas exitosamente")
    @ApiResponse(responseCode = "401", description = "No autenticado")
    public ResponseEntity<List<NotificationResponse>> getMyNotifications(
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.replace("Bearer ", "");
        Long userId = jwtUtils.extractUserId(token);

        List<NotificationResponse> notifications = notificationService.getMyNotifications(userId);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/me/unread-count")
    @Operation(summary = "Contar notificaciones no leídas", description = "Retorna el número de notificaciones no leídas del usuario.")
    @ApiResponse(responseCode = "200", description = "Conteo obtenido exitosamente")
    @ApiResponse(responseCode = "401", description = "No autenticado")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.replace("Bearer ", "");
        Long userId = jwtUtils.extractUserId(token);

        long count = notificationService.countUnread(userId);
        Map<String, Long> response = new HashMap<>();
        response.put("unreadCount", count);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "Marcar como leída", description = "Marca una notificación específica como leída.")
    @ApiResponse(responseCode = "200", description = "Notificación marcada como leída")
    @ApiResponse(responseCode = "401", description = "No autenticado")
    @ApiResponse(responseCode = "404", description = "Notificación no encontrada")
    public ResponseEntity<NotificationResponse> markAsRead(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.replace("Bearer ", "");
        Long userId = jwtUtils.extractUserId(token);

        NotificationResponse response = notificationService.markAsRead(id, userId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar notificación", description = "Elimina permanentemente una notificación del usuario.")
    @ApiResponse(responseCode = "200", description = "Notificación eliminada correctamente")
    @ApiResponse(responseCode = "401", description = "No autenticado")
    @ApiResponse(responseCode = "404", description = "Notificación no encontrada")
    public ResponseEntity<Map<String, String>> deleteNotification(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.replace("Bearer ", "");
        Long userId = jwtUtils.extractUserId(token);

        notificationService.deleteNotification(id, userId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Notificación eliminada correctamente");
        return ResponseEntity.ok(response);
    }
}

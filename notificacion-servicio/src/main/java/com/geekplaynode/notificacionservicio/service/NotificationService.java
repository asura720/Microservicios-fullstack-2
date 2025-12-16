package com.geekplaynode.notificacionservicio.service;

import com.geekplaynode.notificacionservicio.dto.NotificationRequest;
import com.geekplaynode.notificacionservicio.dto.NotificationResponse;
import com.geekplaynode.notificacionservicio.model.Notification;
import com.geekplaynode.notificacionservicio.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    // Crear notificación (llamado por otros microservicios)
    public NotificationResponse createNotification(NotificationRequest request) {
        Notification notification = new Notification();
        notification.setUserId(request.getUserId());
        notification.setTipo(request.getTipo());
        notification.setTitulo(request.getTitulo());
        notification.setMensaje(request.getMensaje());
        notification.setLeida(false);
        
        Notification saved = notificationRepository.save(notification);
        return mapToResponse(saved);
    }
    
    // Obtener notificaciones del usuario
    public List<NotificationResponse> getMyNotifications(Long userId) {
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreadoEnDesc(userId);
        return notifications.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    // Marcar como leída
    public NotificationResponse markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notificación no encontrada"));
        
        if (!notification.getUserId().equals(userId)) {
            throw new RuntimeException("No tienes permiso para marcar esta notificación");
        }
        
        notification.setLeida(true);
        Notification updated = notificationRepository.save(notification);
        return mapToResponse(updated);
    }
    
    // Eliminar notificación
    public void deleteNotification(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notificación no encontrada"));
        
        if (!notification.getUserId().equals(userId)) {
            throw new RuntimeException("No tienes permiso para eliminar esta notificación");
        }
        
        notificationRepository.delete(notification);
    }
    
    // Contar notificaciones no leídas
    public long countUnread(Long userId) {
        return notificationRepository.countByUserIdAndLeida(userId, false);
    }
    
    // Mapper
    private NotificationResponse mapToResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getUserId(),
                notification.getTipo(),
                notification.getTitulo(),
                notification.getMensaje(),
                notification.getLeida(),
                notification.getCreadoEn()
        );
    }
}
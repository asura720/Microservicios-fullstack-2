package com.geekplaynode.notificacionservicio.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private Long id;
    private Long userId;
    private String tipo;
    private String titulo;
    private String mensaje;
    private Boolean leida;
    private LocalDateTime creadoEn;
}
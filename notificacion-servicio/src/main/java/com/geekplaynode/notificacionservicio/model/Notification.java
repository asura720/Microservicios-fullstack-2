package com.geekplaynode.notificacionservicio.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long userId;
    
    @Column(nullable = false)
    private String tipo; // BANEO, ADVERTENCIA, INFO, BIENVENIDA
    
    @Column(nullable = false)
    private String titulo;
    
    @Column(nullable = false, length = 1000)
    private String mensaje;
    
    @Column(nullable = false)
    private Boolean leida = false;
    
    @Column(nullable = false)
    private LocalDateTime creadoEn = LocalDateTime.now();
}
package com.geekplaynode.autenticacionservicio.controller;

import com.geekplaynode.autenticacionservicio.dto.BanUserRequest;
import com.geekplaynode.autenticacionservicio.dto.UserResponse;
import com.geekplaynode.autenticacionservicio.service.AdminService;
import com.geekplaynode.autenticacionservicio.util.JwtUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Administración", description = "API para gestión de usuarios (requiere rol ADMIN)")
public class AdminController {

    private final AdminService adminService;
    private final JwtUtils jwtUtils;

    @GetMapping("/users")
    @Operation(summary = "Obtener todos los usuarios", description = "Lista todos los usuarios registrados en el sistema.")
    @ApiResponse(responseCode = "200", description = "Lista de usuarios obtenida exitosamente")
    @ApiResponse(responseCode = "401", description = "No autenticado - Token no proporcionado")
    @ApiResponse(responseCode = "403", description = "Prohibido - Usuario no es administrador")
    public ResponseEntity<List<UserResponse>> getAllUsers(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).build();
        }

        String token = authHeader.substring(7);
        String role = jwtUtils.extractRole(token);

        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @PostMapping("/users/{userId}/ban")
    @Operation(summary = "Banear usuario", description = "Suspende el acceso de un usuario al sistema especificando una razón.")
    @ApiResponse(responseCode = "200", description = "Usuario baneado exitosamente")
    @ApiResponse(responseCode = "401", description = "No autenticado - Token no proporcionado")
    @ApiResponse(responseCode = "403", description = "Prohibido - Usuario no es administrador")
    @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    public ResponseEntity<Void> banUser(
            @PathVariable Long userId,
            @RequestBody BanUserRequest request,
            HttpServletRequest httpRequest) {

        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).build();
        }

        String token = authHeader.substring(7);
        String role = jwtUtils.extractRole(token);

        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(403).build();
        }

        adminService.banUser(userId, request.getRazon());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/users/{userId}/unban")
    @Operation(summary = "Desbanear usuario", description = "Restaura el acceso de un usuario previamente baneado.")
    @ApiResponse(responseCode = "200", description = "Usuario desbaneado exitosamente")
    @ApiResponse(responseCode = "401", description = "No autenticado - Token no proporcionado")
    @ApiResponse(responseCode = "403", description = "Prohibido - Usuario no es administrador")
    @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    public ResponseEntity<Void> unbanUser(
            @PathVariable Long userId,
            HttpServletRequest httpRequest) {

        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).build();
        }

        String token = authHeader.substring(7);
        String role = jwtUtils.extractRole(token);

        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(403).build();
        }

        adminService.unbanUser(userId);
        return ResponseEntity.ok().build();
    }
}

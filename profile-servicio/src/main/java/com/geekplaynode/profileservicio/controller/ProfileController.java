package com.geekplaynode.profileservicio.controller;

import com.geekplaynode.profileservicio.dto.CreateProfileRequest;
import com.geekplaynode.profileservicio.dto.ProfileResponse;
import com.geekplaynode.profileservicio.dto.UpdateProfileRequest;
import com.geekplaynode.profileservicio.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@Tag(name = "Perfiles", description = "API para gestión de perfiles de usuarios")
public class ProfileController {

    private final ProfileService profileService;

    @PostMapping("/create")
    @Operation(summary = "Crear perfil", description = "Crea un nuevo perfil de usuario (llamado automáticamente al registrarse).")
    @ApiResponse(responseCode = "200", description = "Perfil creado exitosamente")
    @ApiResponse(responseCode = "400", description = "Datos inválidos")
    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    public ResponseEntity<ProfileResponse> createProfile(@RequestBody CreateProfileRequest request) {
        ProfileResponse profile = profileService.createProfile(request);
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/me")
    @Operation(summary = "Ver mi perfil", description = "Obtiene el perfil del usuario autenticado.")
    @ApiResponse(responseCode = "200", description = "Perfil obtenido exitosamente")
    @ApiResponse(responseCode = "401", description = "No autenticado")
    @ApiResponse(responseCode = "404", description = "Perfil no encontrado")
    public ResponseEntity<ProfileResponse> getMyProfile(Authentication authentication) {
        String email = authentication.getName(); // Email viene del token JWT
        ProfileResponse profile = profileService.getMyProfile(email);
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/me")
    @Operation(summary = "Actualizar mi perfil", description = "Actualiza los datos del perfil del usuario autenticado.")
    @ApiResponse(responseCode = "200", description = "Perfil actualizado exitosamente")
    @ApiResponse(responseCode = "401", description = "No autenticado")
    @ApiResponse(responseCode = "404", description = "Perfil no encontrado")
    public ResponseEntity<ProfileResponse> updateMyProfile(
            Authentication authentication,
            @RequestBody UpdateProfileRequest request
    ) {
        String email = authentication.getName();
        ProfileResponse updated = profileService.updateMyProfile(email, request);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Ver perfil por ID", description = "Obtiene el perfil de un usuario específico por su ID.")
    @ApiResponse(responseCode = "200", description = "Perfil obtenido exitosamente")
    @ApiResponse(responseCode = "404", description = "Perfil no encontrado")
    public ResponseEntity<ProfileResponse> getProfileById(@PathVariable Long id) {
        ProfileResponse profile = profileService.getProfileById(id);
        return ResponseEntity.ok(profile);
    }
}
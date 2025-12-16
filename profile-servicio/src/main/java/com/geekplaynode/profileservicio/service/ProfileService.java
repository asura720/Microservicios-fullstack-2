package com.geekplaynode.profileservicio.service;

import com.geekplaynode.profileservicio.dto.CreateProfileRequest;
import com.geekplaynode.profileservicio.dto.ProfileResponse;
import com.geekplaynode.profileservicio.dto.UpdateProfileRequest;
import com.geekplaynode.profileservicio.model.Profile;
import com.geekplaynode.profileservicio.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;

    // 1. CREAR PERFIL (llamado por AuthService cuando alguien se registra)
    @Transactional
    public ProfileResponse createProfile(CreateProfileRequest request) {
        // Verificar si ya existe
        if (profileRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("El perfil con este email ya existe");
        }

        Profile profile = Profile.builder()
                .id(request.getUserId())  // Mismo ID que el User de AuthService
                .nombre(request.getNombre())
                .email(request.getEmail())
                .role(request.getRole())
                .biografia(null)  // Empieza vacío
                .avatarUrl(null)  // Empieza vacío
                .build();

        Profile saved = profileRepository.save(profile);
        return ProfileResponse.fromProfile(saved);
    }

    // 2. OBTENER MI PERFIL (por email del token)
    public ProfileResponse getMyProfile(String email) {
        Profile profile = profileRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Perfil no encontrado"));
        return ProfileResponse.fromProfile(profile);
    }

    // 3. OBTENER PERFIL POR ID (para ver el perfil de otros usuarios)
    public ProfileResponse getProfileById(Long id) {
        Profile profile = profileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Perfil no encontrado"));
        return ProfileResponse.fromProfile(profile);
    }

    // 4. ACTUALIZAR MI PERFIL
    @Transactional
    public ProfileResponse updateMyProfile(String email, UpdateProfileRequest request) {
        Profile profile = profileRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Perfil no encontrado"));

        // Solo actualizamos los campos que vienen en el request
        if (request.getNombre() != null && !request.getNombre().trim().isEmpty()) {
            profile.setNombre(request.getNombre());
        }
        if (request.getBiografia() != null) {
            profile.setBiografia(request.getBiografia());
        }
        if (request.getAvatarUrl() != null) {
            profile.setAvatarUrl(request.getAvatarUrl());
        }

        Profile updated = profileRepository.save(profile);
        return ProfileResponse.fromProfile(updated);
    }

    // 5. VERIFICAR SI UN PERFIL EXISTE (útil para otros microservicios)
    public boolean existsById(Long userId) {
        return profileRepository.existsById(userId);
    }
}
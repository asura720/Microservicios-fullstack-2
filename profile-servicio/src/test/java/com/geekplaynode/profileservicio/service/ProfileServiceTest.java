package com.geekplaynode.profileservicio.service;

import com.geekplaynode.profileservicio.dto.CreateProfileRequest;
import com.geekplaynode.profileservicio.dto.ProfileResponse;
import com.geekplaynode.profileservicio.dto.UpdateProfileRequest;
import com.geekplaynode.profileservicio.model.Profile;
import com.geekplaynode.profileservicio.model.Role;
import com.geekplaynode.profileservicio.repository.ProfileRepository;
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
class ProfileServiceTest {

    @Mock
    private ProfileRepository profileRepository;

    @InjectMocks
    private ProfileService profileService;

    @Test
    void createProfile_DeberiaCrear_CuandoEmailNoExiste() {
        // 1. Arrange
        CreateProfileRequest request = CreateProfileRequest.builder()
                .userId(1L)
                .nombre("Usuario Test")
                .email("test@mail.com")
                .role(Role.USER) // Asumiendo que Role es un Enum
                .build();

        when(profileRepository.existsByEmail(request.getEmail())).thenReturn(false);

        // Simulamos el perfil guardado
        Profile savedProfile = Profile.builder()
                .id(1L)
                .nombre("Usuario Test")
                .email("test@mail.com")
                .role(Role.USER)
                .build();

        when(profileRepository.save(any(Profile.class))).thenReturn(savedProfile);

        // 2. Act
        ProfileResponse response = profileService.createProfile(request);

        // 3. Assert
        assertNotNull(response);
        assertEquals("Usuario Test", response.getNombre());
        verify(profileRepository).save(any(Profile.class));
    }

    @Test
    void createProfile_DeberiaFallar_CuandoEmailYaExiste() {
        // 1. Arrange
        CreateProfileRequest request = CreateProfileRequest.builder()
                .email("repetido@mail.com")
                .build();

        when(profileRepository.existsByEmail("repetido@mail.com")).thenReturn(true);

        // 2. Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            profileService.createProfile(request);
        });

        assertEquals("El perfil con este email ya existe", ex.getMessage());
        verify(profileRepository, never()).save(any(Profile.class));
    }

    @Test
    void getMyProfile_DeberiaRetornarPerfil_SiExiste() {
        // 1. Arrange
        String email = "test@mail.com";
        Profile profile = Profile.builder().id(1L).email(email).nombre("Test").build();

        when(profileRepository.findByEmail(email)).thenReturn(Optional.of(profile));

        // 2. Act
        ProfileResponse response = profileService.getMyProfile(email);

        // 3. Assert
        assertEquals("Test", response.getNombre());
    }

    @Test
    void updateMyProfile_DeberiaActualizarSoloCamposNoNulos() {
        // 1. Arrange
        String email = "test@mail.com";
        
        // Perfil original
        Profile existingProfile = Profile.builder()
                .id(1L)
                .email(email)
                .nombre("Viejo Nombre")
                .biografia("Vieja Bio")
                .build();

        // Request con cambios (solo nombre y bio, avatar nulo)
        UpdateProfileRequest request = new UpdateProfileRequest();
        // Asumiendo setters o builder en UpdateProfileRequest, o usamos un mock si no los tienes accesibles
        // Aquí uso reflexión o asumo que tienes setters. Si tienes @Builder úsalo.
        // Simularemos los getters del request para el test:
        UpdateProfileRequest mockRequest = mock(UpdateProfileRequest.class);
        when(mockRequest.getNombre()).thenReturn("Nuevo Nombre");
        when(mockRequest.getBiografia()).thenReturn("Nueva Bio");
        when(mockRequest.getAvatarUrl()).thenReturn(null); // No actualizamos avatar

        when(profileRepository.findByEmail(email)).thenReturn(Optional.of(existingProfile));
        when(profileRepository.save(any(Profile.class))).thenAnswer(i -> i.getArguments()[0]);

        // 2. Act
        ProfileResponse response = profileService.updateMyProfile(email, mockRequest);

        // 3. Assert
        assertEquals("Nuevo Nombre", response.getNombre());
        assertEquals("Nueva Bio", response.getBiografia());
        // El avatar no debió cambiar (o quedar nulo si era nulo, pero la lógica del servicio no lo toca si es null)
        verify(profileRepository).save(existingProfile);
    }
}
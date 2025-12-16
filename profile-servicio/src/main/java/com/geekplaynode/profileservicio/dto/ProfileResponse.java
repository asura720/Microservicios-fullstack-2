package com.geekplaynode.profileservicio.dto;

import com.geekplaynode.profileservicio.model.Profile;
import com.geekplaynode.profileservicio.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProfileResponse {
    private Long id;
    private String nombre;
    private String email;
    private Role role;
    private String biografia;
    private String avatarUrl;
    private LocalDateTime creadoEn;
    private LocalDateTime actualizadoEn;

    public static ProfileResponse fromProfile(Profile profile) {
        return ProfileResponse.builder()
                .id(profile.getId())
                .nombre(profile.getNombre())
                .email(profile.getEmail())
                .role(profile.getRole())
                .biografia(profile.getBiografia())
                .avatarUrl(profile.getAvatarUrl())
                .creadoEn(profile.getCreadoEn())
                .actualizadoEn(profile.getActualizadoEn())
                .build();
    }
}
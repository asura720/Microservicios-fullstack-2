package com.geekplaynode.profileservicio.dto;

import com.geekplaynode.profileservicio.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateProfileRequest {
    private Long userId;      // El ID que viene de AuthService
    private String nombre;
    private String email;
    private Role role;
}
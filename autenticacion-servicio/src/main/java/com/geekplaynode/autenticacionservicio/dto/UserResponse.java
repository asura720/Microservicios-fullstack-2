package com.geekplaynode.autenticacionservicio.dto;

import com.geekplaynode.autenticacionservicio.model.Role;
import com.geekplaynode.autenticacionservicio.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    private Long id;
    private String nombre;
    private String email;
    private Role role;
    private Boolean baneado;
    private Date createdAt;

    public static UserResponse fromUser(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .nombre(user.getNombre())
                .email(user.getEmail())
                .role(user.getRole())
                .baneado(user.getBaneado())
                .createdAt(user.getCreatedAt())
                .build();
    }
}

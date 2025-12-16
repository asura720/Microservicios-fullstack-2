package com.geekplaynode.profileservicio.repository;

import com.geekplaynode.profileservicio.model.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
    
    // Buscar perfil por email
    Optional<Profile> findByEmail(String email);
    
    // Verificar si existe un perfil con ese email
    boolean existsByEmail(String email);
}
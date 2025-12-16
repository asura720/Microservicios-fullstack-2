package com.geekplaynode.autenticacionservicio.repository;

import com.geekplaynode.autenticacionservicio.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // Método mágico: Spring crea el SQL automáticamente para buscar por email
    Optional<User> findByEmail(String email);
}
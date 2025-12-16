package com.geekplaynode.autenticacionservicio.config;

import com.geekplaynode.autenticacionservicio.model.Role;
import com.geekplaynode.autenticacionservicio.model.User;
import com.geekplaynode.autenticacionservicio.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Solo cargar datos si la BD está vacía
        if (userRepository.count() == 0) {
            System.out.println("🔄 Cargando usuarios de ejemplo...");

            // Usuario Admin
            User admin = User.builder()
                    .nombre("Admin GeekPlay")
                    .email("admin@geekplay.com")
                    .password(passwordEncoder.encode("admin123"))
                    .role(Role.ADMIN)
                    .baneado(false)
                    .build();
            userRepository.save(admin);

            // Usuario normal 1
            User user1 = User.builder()
                    .nombre("Ricardo Gamer")
                    .email("ricardo@test.com")
                    .password(passwordEncoder.encode("123456"))
                    .role(Role.USER)
                    .baneado(false)
                    .build();
            userRepository.save(user1);

            // Usuario normal 2
            User user2 = User.builder()
                    .nombre("Maria Otaku")
                    .email("maria@test.com")
                    .password(passwordEncoder.encode("123456"))
                    .role(Role.USER)
                    .baneado(false)
                    .build();
            userRepository.save(user2);

            System.out.println("✅ Usuarios de ejemplo cargados:");
            System.out.println("   - Admin: admin@geekplay.com / admin123");
            System.out.println("   - User1: ricardo@test.com / 123456");
            System.out.println("   - User2: maria@test.com / 123456");
        }
    }
}
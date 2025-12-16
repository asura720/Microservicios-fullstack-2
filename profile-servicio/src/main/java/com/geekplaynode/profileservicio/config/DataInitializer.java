package com.geekplaynode.profileservicio.config;

import com.geekplaynode.profileservicio.model.Profile;
import com.geekplaynode.profileservicio.model.Role;
import com.geekplaynode.profileservicio.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final ProfileRepository profileRepository;

    @Override
    public void run(String... args) {
        if (profileRepository.count() == 0) {
            System.out.println("🔄 Cargando perfiles de ejemplo...");

            // Perfil Admin (userId: 1)
            Profile adminProfile = Profile.builder()
                    .id(1L)
                    .nombre("Admin GeekPlay")
                    .email("admin@geekplay.com")
                    .role(Role.ADMIN)
                    .biografia("Administrador de la plataforma GeekPlay")
                    .avatarUrl("https://i.pravatar.cc/150?img=33")
                    .build();
            profileRepository.save(adminProfile);

            // Perfil User1 (userId: 2)
            Profile user1Profile = Profile.builder()
                    .id(2L)
                    .nombre("Ricardo Gamer")
                    .email("ricardo@test.com")
                    .role(Role.USER)
                    .biografia("Gamer apasionado por los RPGs y souls-like 🎮")
                    .avatarUrl("https://i.pravatar.cc/150?img=12")
                    .build();
            profileRepository.save(user1Profile);

            // Perfil User2 (userId: 3)
            Profile user2Profile = Profile.builder()
                    .id(3L)
                    .nombre("Maria Otaku")
                    .email("maria@test.com")
                    .role(Role.USER)
                    .biografia("Amante del anime y las series asiáticas 🌸")
                    .avatarUrl("https://i.pravatar.cc/150?img=5")
                    .build();
            profileRepository.save(user2Profile);

            System.out.println("✅ Perfiles de ejemplo cargados");
        }
    }
}
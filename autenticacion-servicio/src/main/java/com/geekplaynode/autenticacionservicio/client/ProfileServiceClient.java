package com.geekplaynode.autenticacionservicio.client;

import com.geekplaynode.autenticacionservicio.model.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;


@Component
@RequiredArgsConstructor
public class ProfileServiceClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${profile.service.url:http://localhost:3002}")
    private String profileServiceUrl;

    public void createProfile(Long userId, String nombre, String email, Role role) {
        try {
            // Crear el objeto de request
            CreateProfileRequest request = new CreateProfileRequest(userId, nombre, email, role);

            // Llamar al ProfileService
            webClientBuilder.build()
                    .post()
                    .uri(profileServiceUrl + "/api/profile/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block(); // Bloquea hasta que termine (síncroníco)

            System.out.println("✅ Perfil creado en ProfileService para: " + email);

        } catch (Exception e) {
            System.err.println("❌ Error al crear perfil en ProfileService: " + e.getMessage());
            // NO lanzamos excepción para que el registro no falle si ProfileService está caído
        }
    }

    // Clase interna para el request
    private record CreateProfileRequest(Long userId, String nombre, String email, Role role) {}
}
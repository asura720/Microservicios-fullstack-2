package com.geekplaynode.autenticacionservicio.service;

import com.geekplaynode.autenticacionservicio.dto.UserResponse;
import com.geekplaynode.autenticacionservicio.model.User;
import com.geekplaynode.autenticacionservicio.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserResponse::fromUser)
                .collect(Collectors.toList());
    }

    @Transactional
    public void banUser(Long userId, String razon) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (user.getBaneado()) {
            throw new RuntimeException("El usuario ya está baneado");
        }

        user.setBaneado(true);
        user.setMotivoBaneo(razon);
        userRepository.save(user);

        // Enviar notificación al usuario (la verá cuando sea desbaneado)
        sendBanNotification(userId, razon);
    }

    @Transactional
    public void unbanUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!user.getBaneado()) {
            throw new RuntimeException("El usuario no está baneado");
        }

        user.setBaneado(false);
        user.setMotivoBaneo(null); // Limpiar el motivo
        userRepository.save(user);
    }

    private void sendBanNotification(Long userId, String razon) {
        try {
            String notificationUrl = "http://localhost:3005/api/notifications/create";

            String jsonPayload = String.format(
                "{\"userId\": %d, \"tipo\": \"BANEO\", \"titulo\": \"Cuenta Suspendida\", \"mensaje\": \"Tu cuenta ha sido suspendida por un administrador. Motivo: %s\"}",
                userId, razon.replace("\"", "\\\"")
            );

            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(notificationUrl))
                    .header("Content-Type", "application/json")
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            System.out.println("📤 Enviando notificación de baneo a usuario " + userId);
            System.out.println("📦 Payload: " + jsonPayload);

            client.sendAsync(request, java.net.http.HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        System.out.println("📥 Respuesta recibida - Status: " + response.statusCode());
                        return response;
                    })
                    .thenAccept(response -> {
                        if (response.statusCode() == 200 || response.statusCode() == 201) {
                            System.out.println("✅ Notificación de baneo enviada exitosamente");
                        } else {
                            System.err.println("⚠️ Error al enviar notificación de baneo. Status: " + response.statusCode());
                        }
                    })
                    .exceptionally(ex -> {
                        System.err.println("❌ Excepción al enviar notificación de baneo: " + ex.getMessage());
                        return null;
                    });
        } catch (Exception e) {
            System.err.println("❌ Error al enviar notificación de baneo: " + e.getMessage());
        }
    }
}

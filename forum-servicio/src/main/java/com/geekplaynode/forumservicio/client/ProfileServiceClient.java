package com.geekplaynode.forumservicio.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Component
public class ProfileServiceClient {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Profile service base URL (configurable via 'profile.service.url' property)
    @Value("${profile.service.url:http://localhost:3002/api/profile}")
    private String profileServiceUrl;

    public ProfileData getProfile(Long userId) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(profileServiceUrl + "/" + userId))
                    .GET()
                    .build();

            System.out.println("🔍 Llamando al servicio de perfiles: " + profileServiceUrl + "/" + userId);

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("📥 Respuesta del servicio de perfiles - Status: " + response.statusCode());
            System.out.println("📦 Body: " + response.body());

            if (response.statusCode() == 200) {
                JsonNode jsonNode = objectMapper.readTree(response.body());
                String nombre = jsonNode.get("nombre").asText();
                String avatar = jsonNode.has("avatarUrl") && !jsonNode.get("avatarUrl").isNull()
                    ? jsonNode.get("avatarUrl").asText()
                    : null;

                System.out.println("✅ Perfil obtenido - Nombre: " + nombre + ", Avatar: " + avatar);
                return new ProfileData(nombre, avatar);
            } else {
                System.err.println("⚠️ Error al obtener perfil. Status: " + response.statusCode());
                System.err.println("⚠️ Response: " + response.body());
                return null;
            }
        } catch (Exception e) {
            System.err.println("❌ Error al llamar al servicio de perfiles: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static class ProfileData {
        private final String nombre;
        private final String avatar;

        public ProfileData(String nombre, String avatar) {
            this.nombre = nombre;
            this.avatar = avatar;
        }

        public String getNombre() {
            return nombre;
        }

        public String getAvatar() {
            return avatar;
        }
    }
}

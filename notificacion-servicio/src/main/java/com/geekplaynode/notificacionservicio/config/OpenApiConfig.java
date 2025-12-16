package com.geekplaynode.notificacionservicio.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("GeekPlay Node - Notificaciones API")
                        .version("1.0.0")
                        .description("API REST para gestión de notificaciones de usuarios en GeekPlay Node.")
                        .contact(new Contact()
                                .name("GeekPlay Node Team")
                                .email("support@geekplaynode.com")
                                .url("https://geekplaynode.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:3005")
                                .description("Servidor de Desarrollo"),
                        new Server()
                                .url("https://api.geekplaynode.com")
                                .description("Servidor de Producción")))
                .components(new Components()
                        .addSecuritySchemes("bearer-jwt", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Ingrese el token JWT obtenido del login")))
                .addSecurityItem(new SecurityRequirement().addList("bearer-jwt"));
    }
}

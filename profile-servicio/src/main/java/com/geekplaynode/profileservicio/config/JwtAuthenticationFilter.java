package com.geekplaynode.profileservicio.config;

import com.geekplaynode.profileservicio.util.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;
        final String role;

        // 1. Verificar si el header tiene el Bearer Token
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Extraer el token
        jwt = authHeader.substring(7);

        try {
            // 3. Validar token
            if (!jwtUtils.isTokenValid(jwt)) {
                filterChain.doFilter(request, response);
                return;
            }

            // 4. Extraer datos del token
            userEmail = jwtUtils.extractUsername(jwt);
            role = jwtUtils.extractRole(jwt);

            // 5. Si hay email y el usuario no está autenticado todavía
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                
                // Crear la autoridad basada en el rol
                SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);
                
                // Crear el token de autenticación
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userEmail,
                        null,
                        List.of(authority)
                );
                
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                // 6. Marcar al usuario como "AUTENTICADO" en el sistema
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        } catch (Exception e) {
            // Si hay error al validar el token, simplemente no autenticamos
            logger.error("Error validando JWT: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
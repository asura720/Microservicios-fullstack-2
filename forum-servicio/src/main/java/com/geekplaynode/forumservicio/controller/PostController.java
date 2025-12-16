package com.geekplaynode.forumservicio.controller;

import com.geekplaynode.forumservicio.client.ProfileServiceClient;
import com.geekplaynode.forumservicio.dto.CreatePostRequest;
import com.geekplaynode.forumservicio.dto.PostResponse;
import com.geekplaynode.forumservicio.dto.UpdatePostRequest;
import com.geekplaynode.forumservicio.service.PostService;
import com.geekplaynode.forumservicio.util.JwtUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@Tag(name = "Posts", description = "API para gestión de publicaciones del foro")
public class PostController {

    private final PostService postService;
    private final JwtUtils jwtUtils;
    private final ProfileServiceClient profileServiceClient;

    @GetMapping
    @Operation(summary = "Listar todos los posts", description = "Obtiene todos los posts del foro (público).")
    @ApiResponse(responseCode = "200", description = "Posts obtenidos exitosamente")
    public ResponseEntity<List<PostResponse>> getAllPosts() {
        return ResponseEntity.ok(postService.getAllPosts());
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Listar posts por categoría", description = "Obtiene los posts de una categoría específica por ID (público).")
    @ApiResponse(responseCode = "200", description = "Posts obtenidos exitosamente")
    @ApiResponse(responseCode = "404", description = "Categoría no encontrada")
    public ResponseEntity<List<PostResponse>> getPostsByCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(postService.getPostsByCategory(categoryId));
    }

    @GetMapping("/category/slug/{slug}")
    @Operation(summary = "Listar posts por slug de categoría", description = "Obtiene los posts de una categoría específica por slug (público).")
    @ApiResponse(responseCode = "200", description = "Posts obtenidos exitosamente")
    @ApiResponse(responseCode = "404", description = "Categoría no encontrada")
    public ResponseEntity<List<PostResponse>> getPostsByCategorySlug(@PathVariable String slug) {
        return ResponseEntity.ok(postService.getPostsByCategorySlug(slug));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener post por ID", description = "Obtiene un post específico por su ID (público).")
    @ApiResponse(responseCode = "200", description = "Post obtenido exitosamente")
    @ApiResponse(responseCode = "404", description = "Post no encontrado")
    public ResponseEntity<PostResponse> getPostById(@PathVariable Long id) {
        return ResponseEntity.ok(postService.getPostById(id));
    }

    @PostMapping
    @Operation(summary = "Crear post", description = "Crea un nuevo post en el foro (requiere autenticación).")
    @ApiResponse(responseCode = "200", description = "Post creado exitosamente")
    @ApiResponse(responseCode = "401", description = "No autenticado")
    @ApiResponse(responseCode = "400", description = "Datos inválidos")
    public ResponseEntity<PostResponse> createPost(
            @RequestBody CreatePostRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        // Extraer userId del token JWT directamente
        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).build();
        }

        String token = authHeader.substring(7);
        Long userId = jwtUtils.extractUserId(token);
        String userEmail = authentication.getName();

        // Obtener nombre y avatar del servicio de perfiles
        String autorNombre = userEmail.split("@")[0]; // Fallback
        String autorAvatar = null;

        System.out.println("📝 Creando post - userId: " + userId + ", email: " + userEmail);
        ProfileServiceClient.ProfileData profile = profileServiceClient.getProfile(userId);
        if (profile != null) {
            autorNombre = profile.getNombre();
            autorAvatar = profile.getAvatar();
            System.out.println("✅ Perfil obtenido - nombre: " + autorNombre + ", avatar: " + autorAvatar);
        } else {
            System.out.println("⚠️ Perfil NO obtenido, usando fallback: " + autorNombre);
        }

        PostResponse post = postService.createPost(request, userId, autorNombre, autorAvatar);
        return ResponseEntity.ok(post);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar post", description = "Actualiza un post existente (solo autor o admin).")
    @ApiResponse(responseCode = "200", description = "Post actualizado exitosamente")
    @ApiResponse(responseCode = "401", description = "No autenticado")
    @ApiResponse(responseCode = "403", description = "Prohibido - No eres el autor ni admin")
    @ApiResponse(responseCode = "404", description = "Post no encontrado")
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable Long id,
            @RequestBody UpdatePostRequest request,
            HttpServletRequest httpRequest
    ) {
    // Extraer del token JWT directamente
    String authHeader = httpRequest.getHeader("Authorization");
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
        return ResponseEntity.status(401).build();
    }
    
    String token = authHeader.substring(7);
    Long userId = jwtUtils.extractUserId(token);
    String role = jwtUtils.extractRole(token);

    PostResponse updated = postService.updatePost(id, request, userId, role);
    return ResponseEntity.ok(updated);
}

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar post", description = "Elimina un post del foro (solo autor o admin).")
    @ApiResponse(responseCode = "204", description = "Post eliminado exitosamente")
    @ApiResponse(responseCode = "401", description = "No autenticado")
    @ApiResponse(responseCode = "403", description = "Prohibido - No eres el autor ni admin")
    @ApiResponse(responseCode = "404", description = "Post no encontrado")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long id,
            @RequestParam(required = false) String reason,
            HttpServletRequest httpRequest) {
        // Extraer del token JWT directamente
        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).build();
        }

        String token = authHeader.substring(7);
        Long userId = jwtUtils.extractUserId(token);
        String role = jwtUtils.extractRole(token);

        postService.deletePost(id, userId, role, reason);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my-posts")
    @Operation(summary = "Obtener mis posts", description = "Obtiene todos los posts del usuario autenticado.")
    @ApiResponse(responseCode = "200", description = "Posts obtenidos exitosamente")
    @ApiResponse(responseCode = "401", description = "No autenticado")
    public ResponseEntity<List<PostResponse>> getMyPosts(HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return ResponseEntity.ok(postService.getPostsByUser(userId));
    }
}
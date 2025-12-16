package com.geekplaynode.forumservicio.controller;

import com.geekplaynode.forumservicio.client.ProfileServiceClient;
import com.geekplaynode.forumservicio.dto.CommentResponse;
import com.geekplaynode.forumservicio.dto.CreateCommentRequest;
import com.geekplaynode.forumservicio.dto.UpdateCommentRequest;
import com.geekplaynode.forumservicio.service.CommentService;
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
@RequestMapping("/api/comments")
@RequiredArgsConstructor
@Tag(name = "Comentarios", description = "API para gestión de comentarios en posts del foro")
public class CommentController {

    private final CommentService commentService;
    private final JwtUtils jwtUtils;
    private final ProfileServiceClient profileServiceClient;

    @GetMapping("/post/{postId}")
    @Operation(summary = "Listar comentarios de un post", description = "Obtiene todos los comentarios de un post específico (público).")
    @ApiResponse(responseCode = "200", description = "Comentarios obtenidos exitosamente")
    @ApiResponse(responseCode = "404", description = "Post no encontrado")
    public ResponseEntity<List<CommentResponse>> getCommentsByPost(@PathVariable Long postId) {
        return ResponseEntity.ok(commentService.getCommentsByPost(postId));
    }

    @PostMapping("/post/{postId}")
    @Operation(summary = "Crear comentario", description = "Crea un nuevo comentario en un post (requiere autenticación).")
    @ApiResponse(responseCode = "200", description = "Comentario creado exitosamente")
    @ApiResponse(responseCode = "401", description = "No autenticado")
    @ApiResponse(responseCode = "404", description = "Post no encontrado")
    public ResponseEntity<CommentResponse> createComment(
            @PathVariable Long postId,
            @RequestBody CreateCommentRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).build();
        }

        String token = authHeader.substring(7);
        Long userId = jwtUtils.extractUserId(token);
        String userEmail = authentication.getName();

        String autorNombre = userEmail.split("@")[0];
        String autorAvatar = null;

        ProfileServiceClient.ProfileData profile = profileServiceClient.getProfile(userId);
        if (profile != null) {
            autorNombre = profile.getNombre();
            autorAvatar = profile.getAvatar();
        }

        CommentResponse comment = commentService.createComment(postId, request, userId, autorNombre, autorAvatar);
        return ResponseEntity.ok(comment);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar comentario", description = "Actualiza un comentario existente (solo autor o admin).")
    @ApiResponse(responseCode = "200", description = "Comentario actualizado exitosamente")
    @ApiResponse(responseCode = "401", description = "No autenticado")
    @ApiResponse(responseCode = "403", description = "Prohibido - No eres el autor ni admin")
    @ApiResponse(responseCode = "404", description = "Comentario no encontrado")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable Long id,
            @RequestBody UpdateCommentRequest request,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        String role = (String) httpRequest.getAttribute("userRole");

        CommentResponse updated = commentService.updateComment(id, request, userId, role);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar comentario", description = "Elimina un comentario del foro (solo autor o admin).")
    @ApiResponse(responseCode = "204", description = "Comentario eliminado exitosamente")
    @ApiResponse(responseCode = "401", description = "No autenticado")
    @ApiResponse(responseCode = "403", description = "Prohibido - No eres el autor ni admin")
    @ApiResponse(responseCode = "404", description = "Comentario no encontrado")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).build();
        }

        String token = authHeader.substring(7);
        Long userId = jwtUtils.extractUserId(token);
        String role = jwtUtils.extractRole(token);

        commentService.deleteComment(id, userId, role);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/post/{postId}/count")
    @Operation(summary = "Contar comentarios", description = "Obtiene el número total de comentarios de un post.")
    @ApiResponse(responseCode = "200", description = "Conteo obtenido exitosamente")
    public ResponseEntity<Long> countComments(@PathVariable Long postId) {
        return ResponseEntity.ok(commentService.countCommentsByPost(postId));
    }
}

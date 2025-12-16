package com.geekplaynode.forumservicio.service;

import com.geekplaynode.forumservicio.client.ProfileServiceClient;
import com.geekplaynode.forumservicio.dto.CreatePostRequest;
import com.geekplaynode.forumservicio.dto.PostResponse;
import com.geekplaynode.forumservicio.dto.UpdatePostRequest;
import com.geekplaynode.forumservicio.model.Category;
import com.geekplaynode.forumservicio.model.Post;
import com.geekplaynode.forumservicio.repository.CategoryRepository;
import com.geekplaynode.forumservicio.repository.CommentRepository;
import com.geekplaynode.forumservicio.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;
    private final CommentRepository commentRepository;
    private final ProfileServiceClient profileServiceClient;

    // Inyectar la URL del servicio de notificaciones desde application.properties
    @Value("${notification.service.url:http://localhost:3005/api/notifications}")
    private String notificationServiceUrl;


    // Cache simple para reducir llamadas repetidas al ProfileService
    private final ConcurrentMap<Long, ProfileServiceClient.ProfileData> profileCache = new ConcurrentHashMap<>();

    // 1. OBTENER TODOS LOS POSTS (ordenados por fecha)
    public List<PostResponse> getAllPosts() {
        return postRepository.findAllByOrderByCreadoEnDesc().stream()
                .map(this::enrichPostResponseWithProfile)
                .collect(Collectors.toList());
    }

    // 2. OBTENER POSTS POR CATEGORÍA
    public List<PostResponse> getPostsByCategory(Long categoryId) {
        return postRepository.findByCategoryIdOrderByCreadoEnDesc(categoryId).stream()
                .map(this::enrichPostResponseWithProfile)
                .collect(Collectors.toList());
    }

    // 3. OBTENER POSTS POR SLUG DE CATEGORÍA
    public List<PostResponse> getPostsByCategorySlug(String slug) {
        return postRepository.findByCategorySlug(slug).stream()
                .map(this::enrichPostResponseWithProfile)
                .collect(Collectors.toList());
    }

    // 4. OBTENER POST POR ID
    public PostResponse getPostById(@NonNull Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post no encontrado"));
        return enrichPostResponseWithProfile(post);
    }

    // 5. CREAR POST
    @Transactional
    public PostResponse createPost(@NonNull CreatePostRequest request, @NonNull Long autorId, String autorNombre, String autorAvatar) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        Post post = Post.builder()
                .titulo(request.getTitulo())
                .contenido(request.getContenido())
                .category(category)
                .autorId(autorId)
                .autorNombre(autorNombre)
                .autorAvatar(autorAvatar)
                .build();

        Post saved = postRepository.save(post);
        return PostResponse.fromPost(saved);
    }

    // 6. ACTUALIZAR POST (solo el autor o admin)
    @Transactional
    public PostResponse updatePost(Long postId, UpdatePostRequest request, Long userId, String role) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post no encontrado"));

        // Verificar permisos
        if (!post.getAutorId().equals(userId) && !"ADMIN".equals(role)) {
            throw new RuntimeException("No tienes permiso para editar este post");
        }

        post.setTitulo(request.getTitulo());
        post.setContenido(request.getContenido());

        Post updated = postRepository.save(post);
        return PostResponse.fromPost(updated);
    }

    // 7. ELIMINAR POST (solo el autor o admin)
    @Transactional
    public void deletePost(@NonNull Long postId, Long userId, String role, String reason) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post no encontrado"));

        // Verificar permisos
        if (!post.getAutorId().equals(userId) && !"ADMIN".equals(role)) {
            throw new RuntimeException("No tienes permiso para eliminar este post");
        }

        // Si es admin eliminando el post de otro usuario, enviar notificación
        if ("ADMIN".equals(role) && !post.getAutorId().equals(userId) && reason != null) {
            sendDeletionNotification(post.getAutorId(), post.getTitulo(), reason, notificationServiceUrl);
        }

        // Eliminar primero todos los comentarios asociados
        commentRepository.deleteAll(commentRepository.findByPostId(postId)); // No es necesario, se puede configurar en cascada

        // Ahora sí eliminar el post
        postRepository.deleteById(postId);
    }

    // 8. OBTENER POSTS DE UN USUARIO
    public List<PostResponse> getPostsByUser(Long userId) {
        return postRepository.findByAutorId(userId).stream()
                .map(this::enrichPostResponseWithProfile)
                .collect(Collectors.toList());
    }

    /**
     * Lógica para enriquecer un PostResponse con datos del perfil, consultando el ProfileService
     * si es necesario. Esto asegura que si los posts fueron creados con fallback (por ejemplo 'ricardo'),
     * el UI muestre el nombre y avatar actuales.
     */
    private PostResponse enrichPostResponseWithProfile(Post post) {
        PostResponse response = PostResponse.fromPost(post);

        // Si no hay avatar o el nombre es claramente un fallback (todo en minúsculas sin espacios),
        // consultar al ProfileService para obtener nombre y avatar más exactos.
        boolean needsProfile = response.getAutorAvatar() == null || response.getAutorAvatar().isBlank();
        boolean nameLooksLikeFallback = response.getAutorNombre() == null || response.getAutorNombre().isBlank() ||
                (response.getAutorNombre().equals(response.getAutorNombre().toLowerCase(Locale.ROOT)) && !response.getAutorNombre().contains(" "));

        if (needsProfile || nameLooksLikeFallback) {
            ProfileServiceClient.ProfileData profile = profileCache.computeIfAbsent(post.getAutorId(), id -> profileServiceClient.getProfile(id));
            if (profile != null) {
                response.setAutorNombre(profile.getNombre());
                response.setAutorAvatar(profile.getAvatar());
            }
        }

        // Agregar el contador de comentarios
        int commentCount = commentRepository.findByPostId(post.getId()).size();
        response.setComentariosCount(commentCount);

        return response;
    }

    // Método para enviar notificación (implementación asíncrona)
    private void sendDeletionNotification(Long targetUserId, String postTitle, String reason, String notificationUrl) {
        try {
            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            String jsonPayload = String.format("{\"userId\": %d, \"message\": \"Tu post '%s' fue eliminado por un administrador. Motivo: %s\"}",
                    targetUserId, postTitle, reason);

            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(notificationUrl))
                    .header("Content-Type", "application/json")
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            System.out.println("📤 Enviando notificación a usuario " + targetUserId);
            System.out.println("📦 Payload: " + jsonPayload);

            client.sendAsync(request, java.net.http.HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        System.out.println("📥 Respuesta recibida - Status: " + response.statusCode());
                        System.out.println("📥 Body: " + response.body());
                        return response;
                    })
                    .thenAccept(response -> {
                        if (response.statusCode() == 200 || response.statusCode() == 201) {
                            System.out.println("✅ Notificación enviada exitosamente al usuario " + targetUserId);
                        } else {
                            System.err.println("⚠️ Error al enviar notificación. Status: " + response.statusCode());
                            System.err.println("⚠️ Response: " + response.body());
                        }
                    })
                    .exceptionally(ex -> {
                        System.err.println("❌ Excepción al enviar notificación: " + ex.getMessage());
                        ex.printStackTrace();
                        return null;
                    });
        } catch (Exception e) {
            System.err.println("❌ Error al construir la petición de notificación: " + e.getMessage());
            // No lanzar excepción para no interrumpir la eliminación del post
        }
    }

}
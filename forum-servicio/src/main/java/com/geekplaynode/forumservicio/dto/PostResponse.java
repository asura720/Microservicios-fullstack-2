package com.geekplaynode.forumservicio.dto;

import com.geekplaynode.forumservicio.model.Post;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PostResponse {
    private Long id;
    private String titulo;
    private String contenido;
    private Long categoryId;
    private String categoryNombre;
    private String categorySlug;
    private Long autorId;
    private String autorNombre;
    private String autorAvatar;
    private LocalDateTime creadoEn;
    private LocalDateTime actualizadoEn;
    private Integer comentariosCount; // Contador de comentarios

    public static PostResponse fromPost(Post post) {
        return PostResponse.builder()
                .id(post.getId())
                .titulo(post.getTitulo())
                .contenido(post.getContenido())
                .categoryId(post.getCategory().getId())
                .categoryNombre(post.getCategory().getNombre())
                .categorySlug(post.getCategory().getSlug())
                .autorId(post.getAutorId())
                .autorNombre(post.getAutorNombre())
                .autorAvatar(post.getAutorAvatar())
                .creadoEn(post.getCreadoEn())
                .actualizadoEn(post.getActualizadoEn())
                .comentariosCount(0) // Se actualiza en el servicio
                .build();
    }
}
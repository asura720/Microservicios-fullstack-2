package com.geekplaynode.forumservicio.dto;

import com.geekplaynode.forumservicio.model.Comment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentResponse {
    private Long id;
    private String contenido;
    private Long postId;
    private Long autorId;
    private String autorNombre;
    private String autorAvatar;
    private LocalDateTime creadoEn;
    private LocalDateTime actualizadoEn;

    public static CommentResponse fromComment(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .contenido(comment.getContenido())
                .postId(comment.getPost().getId())
                .autorId(comment.getAutorId())
                .autorNombre(comment.getAutorNombre())
                .autorAvatar(comment.getAutorAvatar())
                .creadoEn(comment.getCreadoEn())
                .actualizadoEn(comment.getActualizadoEn())
                .build();
    }
}
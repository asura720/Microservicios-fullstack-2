package com.geekplaynode.forumservicio.dto;

import com.geekplaynode.forumservicio.model.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoryResponse {
    private Long id;
    private String nombre;
    private String descripcion;
    private String slug;
    private LocalDateTime creadoEn;

    public static CategoryResponse fromCategory(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .nombre(category.getNombre())
                .descripcion(category.getDescripcion())
                .slug(category.getSlug())
                .creadoEn(category.getCreadoEn())
                .build();
    }
}